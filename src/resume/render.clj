(ns resume.render
  "渲染桥接：Selmer 模板渲染。
  render-page 为 powerpack 的 :powerpack/render-page 回调（按 page 实体分发）；
  render-to-string 供本地流程（--to local/pdf、跳转页）直接渲染。
  关闭变量转义以对齐旧 Jinja2 autoescape=False 行为（混淆 span、
  Markdown 生成的 HTML 需原样输出）。"
  (:require [resume.config :as config]
            [resume.page-data :as page-data]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]))

(def ^:private escape-off (delay (selmer-util/turn-off-escaping!)))

(defn init-renderer!
  "设置模板根目录为当前主题 templates/，并关闭转义。"
  [cfg]
  @escape-off
  (selmer/set-resource-path! (str (config/templates-dir cfg))))

(defn render-to-string
  "直接渲染主题内模板（本地流程）。"
  [cfg template data]
  (init-renderer! cfg)
  (selmer/render-file template data))

;; ---------- powerpack 回调 ----------

(defn- ctx-cfg [context]
  (or (-> context :powerpack/app :resume/ctx :config)
      (throw (ex-info "powerpack context 缺少 :resume/ctx" {}))))

(defn- page-lang [page]
  (some-> page :page/locale name))

(defn render-page
  "powerpack 页面渲染回调：按 :page/kind 分发。
  page 为 DataScript 实体（:page/uri :page/kind :page/locale）。"
  [context page]
  (let [cfg (ctx-cfg context)
        {:keys [font-map server?]} (-> context :powerpack/app :resume/ctx)
        lang (page-lang page)]
    (init-renderer! cfg)
    (case (:page/kind page)
      :page-kind/resume (selmer/render-file "index.html" (page-data/online-data cfg font-map lang))
      :page-kind/print  (selmer/render-file "print.html" (page-data/print-data cfg lang {:server? server?}))
      :page-kind/jump   (selmer/render-file "default.html" (page-data/jump-data cfg))
      (throw (ex-info (str "未知页面类型: " (:page/kind page))
                      {:kind (:page/kind page) :uri (:page/uri page)})))))
