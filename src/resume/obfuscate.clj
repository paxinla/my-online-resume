(ns resume.obfuscate
  "字形混淆：构建期生成 字符→CSS class 映射表与对应 CSS。
  等价旧实现的 mapfont.py + jsfid.css，但 class 后缀改为每次构建由
  seed 派生的随机串（seed 缺省随机），增加被爬取器建立映射库的难度。
  class 前缀语义保留：c- = 字母/符号，n- = 数字，z- = 姓名用字（固定）。"
  (:require [clojure.string :as str])
  (:import (java.security SecureRandom)
           (java.util Random)))

(def ^:private letters
  (concat (map char (range 97 123))   ; a-z
          (map char (range 65 91))    ; A-Z
          [\@ \. \- \_]))

(def ^:private digits (map char (range 48 58)))

;; 姓名用字：class 固定（沿用旧实现），便于跨构建稳定
(def ^:private name-chars
  {\潘 "z-nm1st" \欣 "z-nm2nd" \磊 "z-nm3rd"})

(def ^:private suffix-alphabet "abcdefghijklmnopqrstuvwxyz0123456789")

(defn- new-rng [seed]
  (if (some? seed)
    (Random. (long seed))
    (SecureRandom.)))

(defn- random-suffix [^Random rng]
  (apply str (repeatedly 5 #(nth suffix-alphabet (.nextInt rng (count suffix-alphabet))))))

(defn build-font-map
  "返回 {字符 class} 映射。seed 为 nil 时每次调用生成不同映射。"
  [{:keys [seed]}]
  (let [rng (new-rng seed)]
    (merge name-chars
           (into {} (for [c letters] [c (str "c-" (random-suffix rng))]))
           (into {} (for [c digits]  [c (str "n-" (random-suffix rng))])))))

(defn- escape-content [^Character c]
  (cond
    (= c \\) "\\\\"
    (= c \')  "\\'"
    :else (str c)))

(defn fontmap-css
  "生成字形映射 CSS（等价旧 jsfid.css 的混淆段）。"
  [font-map]
  (str/join "\n"
            (for [[ch cls] (sort-by val font-map)]
              (format ".%s:before {\n    font-family: 'Open Sans', sans-serif;\n    font-weight: 300;\n    content: '%s';\n}\n"
                      cls (escape-content ch)))))

(defn obfuscate-text
  "把文本逐字符替换为空 <span class>；映射外的字符原样保留（等价旧行为）。"
  [font-map ^String s]
  (when s
    (str/join ""
              (for [c (.trim s)]
                (if-let [cls (get font-map c)]
                  (format "<span class=\"%s\"></span>" cls)
                  (str c))))))
