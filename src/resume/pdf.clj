(ns resume.pdf
  "PDF 生成：调用本地 wkhtmltopdf 把 print.html 转为 PDF。
  可执行文件路径由 config.edn 的 :wkhtmltopdf 指定；缺省取系统 PATH。"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [resume.config :as config]
            [resume.util :as u])
  (:import (java.io File)))

(defn- wkhtmltopdf-cmd [cfg]
  (or (:wkhtmltopdf cfg) "wkhtmltopdf"))

(defn generate-pdf!
  "把 src-html 转成 dst-pdf。参数对齐旧 pdfkit options：
  --quiet --encoding UTF-8 --page-size A4 --enable-local-file-access。"
  [cfg {:keys [src-html dst-pdf]}]
  (when (u/exists? dst-pdf)
    (u/delete-rec! dst-pdf))
  (when-not (u/exists? src-html)
    (throw (ex-info (str "源 HTML 不存在: " src-html) {:path src-html})))
  (u/ensure-dir! (.getParent (io/file dst-pdf)))
  (let [{:keys [page-size encoding]} (:pdf-options cfg)
        cmd (->> [(wkhtmltopdf-cmd cfg)
                  "--quiet"
                  "--encoding" (or encoding "UTF-8")
                  "--page-size" (or page-size "A4")
                  "--enable-local-file-access"
                  (str src-html)
                  (str dst-pdf)]
                 (remove nil?)
                 vec)
        pb (ProcessBuilder. ^java.util.List cmd)
        proc (.start pb)
        err-out (slurp (.getErrorStream proc))
        exit (.waitFor proc)]
    (when-not (zero? exit)
      (throw (ex-info (str "wkhtmltopdf 失败，退出码 " exit)
                      {:exit exit
                       :cmd cmd
                       :stderr (str/trim err-out)})))
    (when-not (and (u/exists? dst-pdf) (pos? (.length (io/file dst-pdf))))
      (throw (ex-info (str "wkhtmltopdf 未产出 PDF: " dst-pdf)
                      {:exit exit :stderr (str/trim err-out)})))
    dst-pdf))

(defn generate-for-lang!
  "为单个语言生成 PDF：output/<lang>/print.html → output/<lang>/<pdf-name>。"
  [cfg lang]
  (let [lang (name lang)
        dir (config/output-dir-for cfg lang)]
    (generate-pdf! cfg
                   {:src-html (u/path dir (config/output-file cfg :html-print))
                    :dst-pdf  (u/path dir (config/output-file cfg :pdf-print))})))
