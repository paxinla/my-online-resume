(ns resume.assets
  "资产管线：按主题 assets-manifest.edn 声明的矩阵打包 CSS/JS 并复制图标/图片。
  等价旧实现的 webassets bundle_all + copy_images，另含两项 IE8 兼容处理：
  1. rem → px 回退注入（IE8 不识别 rem，现代浏览器取后值）；
  2. CSS 压缩与占位符替换（如 @@CV-URL-ROOT@@）。
  \"#fontmap#\" 输入哨兵会被替换为本次构建生成的字形混淆 CSS。"
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [resume.config :as config]
            [resume.obfuscate :as obfuscate]
            [resume.util :as u]))

;; ---------- CSS 预处理 ----------

(defn- substitute
  "占位符替换：[[\"@@X@@\" :cv-url-root]] → 用 (:cv-url-root ctx) 替换 @@X@@。"
  [text substitutions ctx]
  (reduce (fn [s [ph k]]
            (str/replace s ph (str (get ctx k ""))))
          text substitutions))

(defn- rem->px
  "16px 基准。返回 \"48px\" 形式（去尾零）。"
  [rem-val]
  (let [n (* 16.0 (Double/parseDouble rem-val))
        v  (int (Math/round n))]
    (str v "px")))

(defn inject-rem-fallback
  "把含 rem 的声明展开为两条：px 在前（IE8 取用），原声明在后（现代浏览器取用）。
   注意不能写成 `padding-left: 32px 2rem`——单值属性双值是非法 CSS，整条会被丢弃。"
  [css]
  (str/replace css
               #"([A-Za-z-]+\s*:\s*)([^;{}]*[0-9]*\.?[0-9]+rem[^;{}]*)"
               (fn [[_ prop vals]]
                 (let [px (str/replace vals #"([0-9]*\.?[0-9]+)rem"
                                       (fn [[_ n]] (str (rem->px n))))]
                   (str prop px ";" prop vals)))))

(defn- minify-css
  "保守 CSS 压缩：去注释、折叠空白、去行尾分号。"
  [css]
  (-> css
      (str/replace #"/\*.*?\*/" "")
      (str/replace #"\s+" " ")
      (str/replace #" ?([{};:,]) ?" "$1")
      (str/replace #";}" "}")
      str/trim))

(defn- process-css
  [css {:keys [substitute? substitutions rem-fallback?]} ctx]
  (cond-> css
    substitute?   (substitute substitutions ctx)
    rem-fallback? (inject-rem-fallback)
    :always       (minify-css)))

(defn- minify-js
  "保守 JS 压缩：主题自有 JS 均按 minification-safe 书写
  （无行注释、无块注释、语句带分号、字符串内无连续空格），
  因此仅需去首尾空白、丢空行、折叠行内连续空格。"
  [js]
  (->> (str/split-lines js)
       (map #(str/replace (str/trim %) #"  +" " "))
       (remove str/blank?)
       (str/join "\n")))

;; ---------- 打包 ----------

(defn load-manifest [cfg]
  (edn/read-string (slurp (config/assets-manifest cfg))))

(defn build!
  "构建全部资产到 target-root（如 output/static 或 resources/static）。
  ctx 为替换上下文（至少含 :cv-url-root）。返回写入的 bundle 文件路径列表。"
  [cfg font-map target-root]
  (let [theme    (config/theme-dir cfg)
        manifest (load-manifest cfg)
        css-ctx  {:cv-url-root (:cv-url-root cfg)}]
    (u/renew-dir! target-root)
    (into []
          (for [{:keys [name inputs substitute]} (:bundles manifest)
                :let [out    (u/path target-root name)
                      is-css (str/ends-with? name ".css")
                      raw    (->> inputs
                                  (map (fn [in]
                                         (if (= "#fontmap#" in)
                                           (obfuscate/fontmap-css font-map)
                                           (slurp (u/path theme in)))))
                                  (str/join "\n"))
                      cooked (if is-css
                               (process-css raw
                                            {:substitute?   (seq substitute)
                                             :substitutions substitute
                                             :rem-fallback? true}
                                            css-ctx)
                               (minify-js raw))]]
        (do (u/ensure-dir! (.getParent (java.io.File. out)))
            (spit out cooked)
            out)))))

(defn copy-static!
  "复制 manifest :copy 声明的目录（icons/img）到 target-root 下。"
  [cfg target-root]
  (let [theme (config/theme-dir cfg)
        manifest (load-manifest cfg)]
    (doseq [[src dst] (:copy manifest)]
      (u/copy-tree! (u/path theme src) (u/path target-root dst)))))
