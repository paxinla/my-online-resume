(ns resume.core
  "CLI 入口。用法（与旧实现等价）：

    clojure -M:run -- --to all|html|pdf|local [--company X] [--job Y] [--server] [--config config.edn]
    clojure -M:run -- dev [--config config.edn]

  --to html   在线站点（根跳转页 + 各语言 index.html/print.html）
  --to pdf    渲染打印页并生成 PDF
  --to all    html + pdf
  --to local  定制版打印页（注入 --company/--job）+ PDF
  --server    打印页引用线上绝对样式（CI 服务器模式）"
  (:gen-class)
  (:require [clojure.string :as str]
            [resume.assets :as assets]
            [resume.config :as config]
            [resume.obfuscate :as obfuscate]
            [resume.page-data :as page-data]
            [resume.pdf :as pdf]
            [resume.powerpack :as powerpack]
            [resume.render :as render]
            [resume.util :as u]))

(def ^:private usage
  "用法: resume [--to all|html|pdf|local] [--company X] [--job Y] [--server] [--config config.edn]
        resume dev [--config config.edn]")

(defn- parse-args [args]
  (loop [args args
         acc {:to nil :company nil :job nil :server? false :config "config.edn" :mode :gen}]
    (if-let [a (first args)]
      (cond
        (= a "--")                        (recur (next args) acc)
        (= a "dev")                       (recur (next args) (assoc acc :mode :dev))
        (= a "--to")                      (recur (nnext args) (assoc acc :to (second args)))
        (= a "--company")                 (recur (nnext args) (assoc acc :company (second args)))
        (= a "--job")                     (recur (nnext args) (assoc acc :job (second args)))
        (= a "--server")                  (recur (next args) (assoc acc :server? true))
        (= a "--config")                  (recur (nnext args) (assoc acc :config (second args)))
        :else                             (throw (ex-info (str "未知参数: " a "\n" usage) {:arg a})))
      acc)))

(defn build-context
  "运行时上下文：config + 字形映射 + server 标志。字形映射每次构建生成一次，
  供资产打包（CSS）与页面渲染（HTML）共用。"
  [{:keys [config server?]}]
  (let [cfg (-> config config/load-config config/validate!)]
    {:config   cfg
     :font-map (obfuscate/build-font-map (:font-obfuscation cfg))
     :server?  (boolean server?)}))

(defn- write-nojekyll! [cfg]
  (spit (u/path (config/output-dir cfg) ".nojekyll") ""))

(defn- render-jump-page!
  "根跳转页（--to html 与 local 均产出，等价旧行为）。"
  [cfg]
  (spit (u/path (config/output-dir cfg) (config/output-file cfg :html-index))
        (render/render-to-string cfg "default.html" (page-data/jump-data cfg))))

(defn- render-print-pages!
  "直接渲染各语言打印页（不经 powerpack，用于 pdf/local 流程）。"
  [{:keys [config server?]}]
  (doseq [lang (:languages config)]
    (let [dir (u/renew-dir! (config/output-dir-for config lang))]
      (spit (u/path dir (config/output-file config :html-print))
            (render/render-to-string config "print.html"
                                     (page-data/print-data config lang
                                                           {:server? server?
                                                            :company nil :job nil}))))))

(defn- render-online!
  "在线站点：powerpack 导出 → 静态资产 → 跳转页兜底 → .nojekyll。
  注意顺序：powerpack 导出会清空 build-dir，资产必须其后写入。"
  [{:keys [config] :as ctx}]
  (u/ensure-dir! (config/output-dir config))
  (powerpack/export! ctx)
  (let [static-root (u/path (config/output-dir config) "static")]
    (assets/build! config (:font-map ctx) static-root)
    (assets/copy-static! config static-root))
  (write-nojekyll! config))

(defn- gen-pdf-all!
  [{:keys [config]}]
  (doseq [lang (:languages config)]
    (pdf/generate-for-lang! config lang)))

(defn- run-flow!
  [{:keys [to company job] :as opts}]
  (let [ctx (build-context opts)
        cfg (:config ctx)]
    (render/init-renderer! cfg)
    (case to
      "html"  (render-online! ctx)

      "pdf"   (do (u/ensure-dir! (config/output-dir cfg))
                  (let [static-root (u/path (config/output-dir cfg) "static")]
                    (assets/build! cfg (:font-map ctx) static-root)
                    (assets/copy-static! cfg static-root))
                  (render-print-pages! ctx)
                  (gen-pdf-all! ctx))

      "local" (do (when (or (str/blank? (str company)) (str/blank? (str job)))
                    (throw (ex-info "--to local 需要 --company 与 --job" {:company company :job job})))
                  (u/ensure-dir! (config/output-dir cfg))
                  (let [static-root (u/path (config/output-dir cfg) "static")]
                    (assets/build! cfg (:font-map ctx) static-root)
                    (assets/copy-static! cfg static-root))
                  ;; 本地模式：注入公司/职位，离线样式，项目裁剪
                  (doseq [lang (:languages cfg)]
                    (let [dir (u/ensure-dir! (config/output-dir-for cfg (name lang)))]
                      (spit (u/path dir (config/output-file cfg :html-print))
                            (render/render-to-string cfg "print.html"
                                                     (page-data/print-data cfg (name lang)
                                                                           {:server? false
                                                                            :company company
                                                                            :job job})))))
                  (render-jump-page! cfg)
                  (gen-pdf-all! ctx))

      "all"   (do (render-online! ctx) (gen-pdf-all! ctx))

      (throw (ex-info (str "不支持的 --to 目标: " (pr-str to) "\n" usage) {:to to})))))

(defn -main
  [& args]
  (try
    (let [opts (parse-args args)]
      (if (= :dev (:mode opts))
        (do
          (require 'resume.powerpack)
          (let [ctx (assoc (build-context opts) :dev? true)
                cfg (:config ctx)
                static-root "resources/static"]
            (assets/build! cfg (:font-map ctx) static-root)
            (assets/copy-static! cfg static-root)
            ((ns-resolve 'resume.powerpack 'start-dev!) ctx))
          ;; dev 服务器阻塞运行，Ctrl-C 退出
          :dev-started)
        (do (run-flow! opts)
            (println "完成。"))))
    (catch Exception e
      (binding [*out* *err*]
        (println "错误:" (.getMessage e))
        (when-let [d (ex-data e)]
          (prn d)))
      (System/exit 1))))
