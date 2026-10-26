(ns resume.content
  "内容层：加载 content/ 下的 EDN / Markdown / text 简历内容。
  等价旧实现的 util.py 各 read_* 函数 + rule.py 的 gen_* 函数。"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [markdown.core :as md]
            [resume.config :as config]
            [resume.util :as u])
  (:import (java.util Base64)))

(defn load-edn
  "读取 EDN 文件为 Clojure 数据。"
  [^String path]
  (when-not (u/exists? path)
    (throw (ex-info (str "内容文件不存在: " path) {:path path})))
  (edn/read-string (slurp path)))

(defn decode-base64 [^String s]
  (String. (.decode (Base64/getDecoder) s) "UTF-8"))

(defn load-banner
  "banner 内容：解码 base64 字段，保留原始 base64 供打印版使用真实手机号。"
  [cfg lang]
  (let [data (load-edn (u/path (config/content-dir-for cfg lang) "banner_info.edn"))]
    (assoc data
           :my-name-value    (decode-base64 (:my-name data))
           :my-email-value   (decode-base64 (:my-email data))
           :my-mobile-m-value (decode-base64 (:my-mobile-m data))
           :my-mobile-r-value (decode-base64 (:my-mobile-r data)))))

(defn load-announce
  "特别声明：逐行读取，去首尾空白（等价 read_text_as_list）。"
  [cfg lang]
  (-> (u/path (config/content-dir-for cfg lang) "announce.text")
      slurp
      str/split-lines
      (#(map str/trim %))
      vec))

(defn md->html
  "Markdown → HTML，并把围栏代码块归一化为 <p><code>：
   旧实现的 python-markdown（无 fenced_code 扩展）把 ``` 围栏渲染为
   列表外的段落内行内 code —— 段落天然自动折行、继承 .project-story p 的缩进；
   markdown-clj 则生成 <pre><code> 且将后续段落吸入列表项 <li> 内，
   导致不折行与缩进偏移。两步转换后与原站 DOM 等价。"
  [md]
  (-> (md/md-to-html-string md)
      ;; 1. pre → 段落内行内 code
      (str/replace #"<pre><code>(?s)(.*?)</code></pre>" "<p><code>$1</code></p>")
      ;; 2. 紧跟列表项的围栏段落移出列表（原站即如此，避免叠加 li 缩进）；
      ;;    </p> 与 </li> 间可能有空白文本节点（源 md 围栏尾随空格），需容忍
      (str/replace #"<li>([^<]*)<p><code>(?s)(.*?)</code></p>\s*</li></ul>"
                   "<li>$1</li></ul><p><code>$2</code></p>")))

(defn load-introduction
  "个人简介：Markdown → HTML。"
  [cfg lang]
  (md->html (slurp (u/path (config/content-dir-for cfg lang) "introduction.md"))))

(defn load-tech-stack
  "技术栈：level 保持整数，星级序列由渲染期按 full-skill-rank 推导。"
  [cfg lang]
  (load-edn (u/path (config/content-dir-for cfg lang) "tech_stack.edn")))

(defn load-jobs       [cfg lang] (load-edn (u/path (config/content-dir-for cfg lang) "job.edn")))
(defn load-education  [cfg lang] (load-edn (u/path (config/content-dir-for cfg lang) "education.edn")))
(defn load-certification [cfg lang] (load-edn (u/path (config/content-dir-for cfg lang) "certification.edn")))
(defn load-foot       [cfg lang] (load-edn (u/path (config/content-dir-for cfg lang) "foot.edn")))

(def ^:private project-exts #{".md" ".mkd" ".markdown"})

(defn- project-ordinal [^java.io.File f]
  (when-let [[_ n] (re-find #"p(\d+)\." (.getName f))]
    (Long/parseLong n)))

(defn load-projects
  "项目经历：projects/ 下 pN.md 按序号降序（新项目在前，等价旧排序），转 HTML。
  返回 [{:ordinal n :project_content html}]。"
  [cfg lang]
  (let [dir (io/file (u/path (config/content-dir-for cfg lang) "projects"))]
    (when-not (.isDirectory dir)
      (throw (ex-info (str "项目目录不存在: " dir) {:dir (str dir)})))
    (->> (.listFiles dir)
         (filter (fn [^java.io.File f]
                   (and (.isFile f)
                        (some #(.endsWith (.getName f) %) project-exts))))
         (map (fn [^java.io.File f]
                {:ordinal       (project-ordinal f)
                 :project_content (md->html (slurp f))}))
         (sort-by :ordinal >)
         vec)))
