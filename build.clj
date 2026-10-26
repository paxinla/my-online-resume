(ns build
  "fatjar 构建：clojure -T:build uber → target/resume-standalone.jar"
  (:require [clojure.tools.build.api :as b]))

(def lib       'resume/online-resume)
(def version   "1.0.0")
(def class-dir "target/classes")
(def uber-file (format "target/%s-standalone-%s.jar" (name lib) version))
(def basis     (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"})
  (println "cleaned target/"))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis     basis
                  :class-dir class-dir
                  :src-dirs  ["src"]
                  :ns-compile '[resume.core]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis     basis
           :main      'resume.core})
  (println "built" uber-file))
