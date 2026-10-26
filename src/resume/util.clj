(ns resume.util
  "文件系统与字符串小工具。"
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)
           (java.nio.file Files StandardCopyOption)))

(defn path
  "拼接路径片段，等价旧实现的 os.path.join。"
  [& parts]
  (let [s (->> parts
               (remove nil?)
               (map str)
               (str/join File/separator))]
    (.getPath (io/file s))))

(defn exists? [^String p]
  (.exists (io/file p)))

(defn ensure-dir! [^String p]
  (.mkdirs (io/file p))
  p)

(defn delete-rec!
  "递归删除文件/目录（不存在则静默）。"
  [^String p]
  (let [f (io/file p)]
    (when (.exists f)
      (if (.isDirectory f)
        (doseq [c (.listFiles f)]
          (delete-rec! (.getPath c)))
        nil)
      (.delete f))))

(defn renew-dir!
  "等价旧实现 renew_path：存在即删，再重建。"
  [^String p]
  (delete-rec! p)
  (ensure-dir! p))

(defn copy-tree!
  "等价旧实现 copy_dirs：把 src 目录整体复制到 dst。"
  [^String src ^String dst]
  (let [src-file (io/file src)
        dst-file (io/file dst)]
    (renew-dir! dst)
    (doseq [^File f (file-seq src-file)
            :when (.isFile f)]
      (let [rel (subs (.getPath f) (inc (count (.getPath src-file))))
            target (io/file dst-file rel)]
        (ensure-dir! (.getParent target))
        (Files/copy (.toPath f) (.toPath target)
                    (into-array StandardCopyOption [StandardCopyOption/REPLACE_EXISTING]))))))
