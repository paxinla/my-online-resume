(ns resume.config
  "config.edn 的读取、默认值合并与校验。
  所有可配置项集中在 config.edn，经命令行 --config 传入，程序其余部分
  只通过本模块的访问器取路径，绝不硬编码。"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [resume.util :as u]))

(def defaults
  {:content-dir     "content"
   :themes-dir      "themes"
   :theme           "default-theme"
   :output-dir      "output"
   :wkhtmltopdf     nil
   :pdf-options     {:page-size "A4"
                     :encoding  "UTF-8"}
   :site-title      "Pxx's Resume"
   :cv-url-root     "https://paxinla.github.io/my-online-resume"
   :github-url      "https://paxinla.github.io"
   ;; 顺序即优先级（降序）：第一个语言为源语言/默认语言（cn），
   ;; 其余语言内容均由它翻译而来。第一个语言生成根跳转页。
   :languages       ["cn" "en"]
   :output-files    {:html-index "index.html"
                     :html-print "print.html"
                     :pdf-print  "resume_data_engineer_pxx.pdf"}
   :dev-server      {:port 4242}
   :full-skill-rank 5
   :font-obfuscation {:enabled true
                      :seed   nil}
   :project-reduction []})

(defn- deep-merge [& ms]
  (apply merge-with (fn [a b] (if (and (map? a) (map? b)) (deep-merge a b) b)) ms))

(defn load-config
  "读取 config.edn 并合并默认值。:root 记录配置文件所在目录，作为全部相对路径的基准。"
  [path]
  (let [f (io/file path)]
    (when-not (.exists f)
      (throw (ex-info (str "配置文件不存在: " (.getPath f)) {:config path})))
    (let [raw (edn/read-string (slurp f))
          cfg (-> (deep-merge defaults raw)
                  (assoc :root (-> f .getAbsoluteFile .getParent)))]
      cfg)))

(defn- abs-path [cfg rel]
  (let [f (io/file rel)]
    (if (.isAbsolute f) rel (u/path (:root cfg) rel))))

(defn content-dir [cfg] (abs-path cfg (:content-dir cfg)))
(defn themes-dir [cfg] (abs-path cfg (:themes-dir cfg)))
(defn theme-dir [cfg] (abs-path cfg (u/path (:themes-dir cfg) (:theme cfg))))
(defn output-dir [cfg] (abs-path cfg (:output-dir cfg)))

(defn content-dir-for [cfg lang] (abs-path cfg (u/path (:content-dir cfg) (name lang))))
(defn output-dir-for [cfg lang] (abs-path cfg (u/path (:output-dir cfg) (name lang))))
(defn templates-dir [cfg] (u/path (theme-dir cfg) "templates"))
(defn assets-manifest [cfg] (u/path (theme-dir cfg) "assets-manifest.edn"))
(defn i18n-file [cfg] (u/path (theme-dir cfg) "i18n.edn"))

(defn output-file [cfg k]
  (get-in cfg [:output-files k] (get-in defaults [:output-files k])))

(defn validate! [cfg]
  (when (empty? (:languages cfg))
    (throw (ex-info "config.edn: :languages 不能为空" {:languages (:languages cfg)})))
  ;; 防御性校验（不阻断）：源语言 cn 应排在首位
  (when-not (= "cn" (-> cfg :languages first str/lower-case))
    (binding [*out* *err*]
      (println "WARN: :languages 首元素不是 \"cn\"。cn 是源语言，应具有最高优先级。")))
  cfg)
