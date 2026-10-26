(ns resume.powerpack
  "powerpack 集成：
  - make-config 构造 powerpack app 配置（页面经 :powerpack/on-ingested 回调注册，
    无需内容种子文件）；
  - export! 导出在线站点到 output/；
  - start-dev! 启动 powerpack 开发服务器（热重载）。
  资产不走 optimus（img/script 的 URL 重写置为可选、不改写），
  由 resume.assets 在导出后写入 output/static，保持旧文件名与相对路径。"
  (:require [datomic-type-extensions.api :as d]
            [powerpack.dev :as dev]
            [powerpack.export :as export]
            [resume.config :as config]
            [resume.render :as render]
            [resume.util :as u]))

(defn page-tx
  "注册页面：根跳转页 + 每个语言的在线页与打印页。
  打印页 URI 带扩展名（/cn/print.html），stasis 可直接导出为单文件。"
  [cfg]
  (into [{:page/uri "/" :page/kind :page-kind/jump}]
        (for [lang (:languages cfg)
              m [{:page/uri (str "/" (name lang) "/") :page/kind :page-kind/resume :page/locale (keyword (name lang))}
                 {:page/uri (str "/" (name lang) "/" (config/output-file cfg :html-print))
                  :page/kind :page-kind/print :page/locale (keyword (name lang))}]]
          m)))

(def ^:dynamic *current-cfg* nil)

(defn ^:private register-pages
  ":powerpack/on-ingested 回调：内容摄取完成后注册页面。
  项目不用 powerpack 的内容管线，页面集合完全由 config 决定。
  :page/uri 带 unique identity，重复注册为 upsert，幂等。"
  [powerpack _results]
  @(d/transact (:datomic/conn powerpack) (page-tx *current-cfg*)))

(defn make-config
  "构造 powerpack app 配置。ctx 为运行时上下文 {:config ... :font-map ... :server? ...}。"
  [{:keys [config font-map server? dev?] :as ctx}]
  (alter-var-root #'*current-cfg* (constantly config))
  (cond-> {:site/title                    (:site-title config)
   :site/base-url                 (:cv-url-root config)
   :site/default-locale           :zh
   :datomic/schema-file             "resources/schema.edn"
   ;; 不用 powerpack 内容管线：后缀列表永不匹配，ingest-all 空转后
   ;; 仍会触发 on-ingested 回调，由它注册页面
   :powerpack/content-dir           (config/content-dir config)
   :powerpack/content-file-suffixes ["__none__"]
   :powerpack/build-dir             (config/output-dir config)
   :powerpack/port                  (get-in config [:dev-server :port])
   :powerpack/render-page           #'resume.render/render-page
   ;; img/script 的 URL 引用原样保留（可选目标不做重写、不校验存在性）：
   ;; 资产由 resume.assets 在导出后复制，文件名与旧实现保持一致
   :powerpack/asset-targets         [{:selector ["img[src]"], :attr "src", :optional? true}
                                     {:selector ["script[src]"], :attr "src", :optional? true}]
   :powerpack/on-ingested           #'resume.powerpack/register-pages
   :powerpack/resource-dirs         ["resources"]
   :powerpack/source-dirs           ["src"]
   :powerpack/dev-assets-root-path  "static"
   :powerpack/log-level             :info
   :resume/ctx                      {:config config :font-map font-map :server? server?}}
   ;; dev 模式：经 optimus 提供静态资产（live），URL 前缀保持 /static/**
   dev?  (assoc :optimus/assets [{:public-dir "resources"
                                  :paths [#"^/static/(css|js|img|icons)/"]}])))

(defn export!
  "导出在线站点到 :output-dir。powerpack 会先备份再清空 build-dir，
  因此静态资产复制必须在导出完成之后进行（由 resume.core 编排）。"
  [ctx]
  (let [result (export/export (make-config ctx))]
    (when-not (:success? result)
      (throw (ex-info "powerpack 导出失败" {:result result})))))

;; ---------- dev server ----------

(def ^:private ^:dynamic *dev-ctx* nil)
(def ^:private dev-started? (atom false))

(defmethod dev/configure! nil []
  (assert *dev-ctx* "start-dev! 未初始化")
  (make-config *dev-ctx*))

(defn start-dev!
  "启动 powerpack 开发服务器（integrant + 热重载）。
  资产先构建到 resources/static（classpath），供 /static/** 访问。
  启动后在标准输出打印各语言预览地址。"
  [ctx]
  (alter-var-root #'*dev-ctx* (constantly ctx))
  (if @dev-started?
    (dev/reset)
    (do (dev/start) (reset! dev-started? true)))
  (let [{:keys [config]} ctx
        port (get-in config [:dev-server :port])
        langs (:languages config)]
    ;; 等 powerpack 异步 logger 先落盘，避免输出交织
    (Thread/sleep 500)
    (println)
    (println "简历预览已就绪：")
    (doseq [lang langs]
      (println (str "  http://localhost:" port "/" (name lang) "/")))
    (println (str "（默认语言: " (first langs) "，即第一个地址；Ctrl-C 退出）"))))

(defn stop-dev! []
  (dev/stop))
