(ns resume.page-data
  "渲染数据装配：等价旧实现 rule.py 的 get_title_words/gen_banner/render_html
  数据段。产出模板消费的渲染 map——键名沿用旧模板契约（snake_case）。"
  (:require [resume.config :as config]
            [resume.content :as content]
            [resume.obfuscate :as obfuscate]
            [resume.util :as u]))

;; ---------- i18n ----------

(defn load-i18n [cfg]
  (content/load-edn (config/i18n-file cfg)))

(defn title-words
  "等价旧 get_title_words：把嵌套 i18n 展平为模板变量。"
  [i18n lang]
  (let [lang (keyword lang)
        _ (when-not (get i18n lang)
            (throw (ex-info (str "i18n 缺少语言 " lang) {:lang lang})))
        src (get i18n lang)
        basic (:basicinfo src)]
    {:banner_contact       (:contact basic)
     :banner_jobexp        (:work-expect basic)
     :work_exp_job         (:work-exp-job basic)
     :work_exp_salary      (:work-exp-salary basic)
     :work_exp_city        (:work-exp-city basic)
     :current_state        (:current-status basic)
     :announce_title       (:announcement src)
     :intro_title          (:introduction src)
     :jobs_title           (:jobs src)
     :edu_title            (:education src)
     :word_from            (:word-from src)
     :word_from_2          (:word-from-2 src)
     :word_to              (:word-to src)
     :year_unit            (:year-unit src)
     :year_unit_4prt       (:year-unit-4prt src)
     :skill_title          (:technology src)
     :skill_main_title     (:technology-main src)
     :skill_other_title    (:technology-other src)
     :cert_title           (:certification src)
     :project_title        (:projects src)
     :view_online_version  (:view-online-version src)
     :target_company       (:target-company src)
     :target_jobname       (:target-jobname src)}))

;; ---------- banner ----------

(defn- avatar-online-url [cfg lang]
  (str (:cv-url-root cfg) "/static/img/" (:my-avatar (content/load-banner cfg lang))))

(defn- avatar-offline-path [cfg lang]
  (str "file:///" (u/path (config/output-dir cfg) "static" "img"
                          (:my-avatar (content/load-banner cfg lang)))))

(defn banner-online
  "在线版 banner：姓名/手机/邮箱过字形混淆，头像用线上 URL。"
  [cfg font-map lang]
  (let [b (content/load-banner cfg lang)]
    {:myname          (obfuscate/obfuscate-text font-map (:my-name-value b))
     :mydomain        (:my-domain b)
     :mymobile        (obfuscate/obfuscate-text font-map (:my-mobile-m-value b))
     :myemail         (obfuscate/obfuscate-text font-map (:my-email-value b))
     :myavatar        (avatar-online-url cfg lang)
     :my_exp_position (:my-exp-position b)
     :my_exp_salary   (:my-exp-salary b)
     :my_exp_workcity (:my-exp-workcity b)
     :mystatus        (:my-status b)
     :mywelcome       (:my-welcome b)
     :mygithub        (:github-url cfg)
     :pdf_version     (str (:cv-url-root cfg) "/" (name lang) "/" (config/output-file cfg :pdf-print))
     :html_version    (str (:cv-url-root cfg) "/" (name lang) "/")}))

(defn banner-print
  "打印版 banner：真实手机号；头像离线路径（模板实际用 style_prefix 引用）。"
  [cfg lang]
  (let [b (content/load-banner cfg lang)]
    {:myname          (:my-name-value b)
     :mydomain        (:my-domain b)
     :mymobile        (:my-mobile-r-value b)
     :myemail         (:my-email-value b)
     :myavatar        (avatar-offline-path cfg lang)
     :my_exp_position (:my-exp-position b)
     :my_exp_salary   (:my-exp-salary b)
     :my_exp_workcity (:my-exp-workcity b)
     :mystatus        (:my-status b)
     :mywelcome       (:my-welcome b)
     :mygithub        (:github-url cfg)
     :pdf_version     (str (:cv-url-root cfg) "/" (name lang) "/" (config/output-file cfg :pdf-print))
     :html_version    (str (:cv-url-root cfg) "/" (name lang) "/")}))

;; ---------- 技术栈 ----------

(defn- skill-with-stars [{:keys [name level]} full-rank]
  (let [lvl (or level 0)]
    {:name        name
     :level       (when (pos? lvl) (range lvl))   ; nil → 模板 {% if %} 跳过
     :rest_level  (range (- full-rank lvl))}))

(defn tech-stack-data [cfg lang]
  (let [{:keys [main-skills other-skills]} (content/load-tech-stack cfg lang)
        full-rank (:full-skill-rank cfg)]
    {:main_skills  (mapv #(skill-with-stars % full-rank) main-skills)
     :other_skills (seq other-skills)}))    ; nil → 模板 {% if %} 跳过

;; ---------- 项目裁剪 ----------

(defn reduce-projects
  "打印版裁剪项目：:project-reduction 里的序号（1 起，对应 pN）被剔除。"
  [projects reduction]
  (let [masked (set reduction)]
    (remove #(contains? masked (:ordinal %)) projects)))

;; ---------- 完整数据 ----------

(defn- cert-with-template-keys [{:keys [short-name] :as cert}]
  (cond-> cert
    (contains? cert :short-name) (assoc :short_name short-name :short-name short-name)))

(defn base-data
  "语言维度的公共数据（在线/打印共用）。"
  [cfg lang]
  (let [lang (name lang)
        cert (content/load-certification cfg lang)]
    {:html_meta  {:title_name (:site-title cfg)}
     :section    (title-words (load-i18n cfg) lang)
     :statements (content/load-announce cfg lang)
     :intro_text (content/load-introduction cfg lang)
     :stack      (tech-stack-data cfg lang)
     :job        (content/load-jobs cfg lang)
     :edu        (content/load-education cfg lang)
     :cert       (update cert :items #(mapv cert-with-template-keys %))
     :projects   (content/load-projects cfg lang)
     :endnote    (content/load-foot cfg lang)}))

(defn online-data
  "在线浏览版：字形混淆 + 线上头像。"
  [cfg font-map lang]
  (-> (base-data cfg lang)
      (assoc :banner (banner-online cfg font-map lang))))

(defn print-data
  "打印版：
  - server?  为 true 时引用线上绝对样式（供 CI 在服务器上出 PDF）；
  - company/job 注入应聘公司/职位（旧 --to local 语义）；
  - 项目经历按 :project-reduction 裁剪。"
  [cfg lang {:keys [server? company job] :or {server? false}}]
  (let [data (base-data cfg lang)]
    (cond-> (assoc data
                   :banner (banner-print cfg lang)
                   :style_prefix (if server? (:cv-url-root cfg) "..")
                   :style_css    (if server? "mycv-print-online.css" "mycv-print.css")
                   :projects (vec (reduce-projects (:projects data) (:project-reduction cfg))))
      company (assoc :target_company company)
      job     (assoc :target_jobname job))))

(defn jump-data
  "根跳转页数据：指向默认语言（:languages 第一个，cn 优先级最高）。"
  [cfg]
  (let [default-lang (first (:languages cfg))]
    {:default_cv_html (str (:cv-url-root cfg) "/" default-lang "/")
     :html_meta       {:title_name (:site-title cfg)}}))
