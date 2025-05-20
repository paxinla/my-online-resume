#!/usr/bin/env python
#coding=utf-8


from jinja2 import Environment, FileSystemLoader
from webassets import Environment as WebEnv
from webassets import Bundle

from config import CONFIG
from mapfont import FontElements
from util import locate_file, decode_base64, read_json_as_map, read_text_as_list, read_markdown_as_html, html2pdf, renew_path, copy_dirs, join_path, list_markdown_files
from conf_projects_reduction import ignore_project_item_indexes


TEMPLATE_ENVIRONMENT = Environment(autoescape=False,
                                   loader=FileSystemLoader(CONFIG.TEMPLATE_PATH),
                                   trim_blocks=False)


def get_title_words(template_path, lang_name):
    """模板中的标题标签等内容"""
    src_title_json = locate_file(template_path, "section_titles.json")
    title_words_map = read_json_as_map(src_title_json)
    if not title_words_map or lang_name not in title_words_map:
        raise ValueError(f"FAIL to load title words in language {lang_name} from file {src_title_json} !")

    rs = {}
    src = title_words_map[lang_name]

    rs["banner_contact"] = src["basicinfo"]["contact"]
    rs["banner_jobexp"] = src["basicinfo"]["work_expect"]
    rs["work_exp_job"] =  src["basicinfo"]["work_exp_job"]
    rs["work_exp_salary"] = src["basicinfo"]["work_exp_salary"]
    rs["work_exp_city"] = src["basicinfo"]["work_exp_city"]
    rs["current_state"] = src["basicinfo"]["current_status"]
    rs["announce_title"] = src["announcement"]
    rs["intro_title"] = src["introduction"]
    rs["jobs_title"] = src["jobs"]
    rs["edu_title"] = src["education"]
    rs["word_from"] = src["word_from"]
    rs["word_from_2"] = src["word_from_2"]
    rs["word_to"] = src["word_to"]
    rs["year_unit"] = src["year_unit"]
    rs["year_unit_4prt"] = src["year_unit_4prt"]
    rs["skill_title"] = src["technology"]
    rs["skill_main_title"] = src["technology_main"]
    rs["skill_other_title"] = src["technology_other"]
    rs["cert_title"] = src["certification"]
    rs["project_title"] = src["projects"]
    rs["view_online_version"] = src["view_online_version"]
    rs["target_company"] = src["target_company"]
    rs["target_jobname"] = src["target_jobname"]

    return rs


def gen_banner(content_path, lang_name):
    """顶部 Banner"""
    banner_data = read_json_as_map(locate_file(content_path, "banner_info.json"))

    # 定位头像
    banner_data["myavatar_online"] = locate_file(CONFIG.IMG_PATH_GITHUB, banner_data["myavatar"])
    banner_data["myavatar_offline"] = "file:///" + locate_file(CONFIG.IMG_PATH_LOCAL, banner_data["myavatar"])

    banner_data["myname"] = decode_base64(banner_data["myname"])
    banner_data["myemail"] = decode_base64(banner_data["myemail"])

    banner_data["mygithub"] = CONFIG.GITHUB_URL
    banner_data["pdf_version"] = CONFIG.VERSION_URL[lang_name]["PDF_VERSION_URL"]
    banner_data["html_version"] = CONFIG.VERSION_URL[lang_name]["HTML_VERSION_URL"]

    return banner_data


def gen_announce(content_path):
    """特别声明"""
    return read_text_as_list(locate_file(content_path, "announce.text"))


def gen_introduction(content_path):
    """个人简介"""
    return read_markdown_as_html(locate_file(content_path, "introduction.md"))


def gen_stack(content_path):
    """技术栈"""
    stack_data = read_json_as_map(locate_file(content_path, "tech_stack.json"))

    for each_main_skill in stack_data["main_skills"]:
        level_num = int(each_main_skill["level"])
        each_main_skill["level"] = range(level_num)
        each_main_skill["rest_level"] = range(CONFIG.FULL_SKILL_RANK-level_num)

    return stack_data


def gen_job_history(content_path):
    """工作经历"""
    return read_json_as_map(locate_file(content_path, "job.json"))


def gen_education_history(content_path):
    """教育经历"""
    return read_json_as_map(locate_file(content_path, "education.json"))


def gen_certification(content_path):
    """资格证书"""
    return read_json_as_map(locate_file(content_path, "certification.json"))


def gen_project_history(content_path):
    """项目经历"""
    all_markdowns = sorted(list_markdown_files(locate_file(content_path, "projects")), reverse=True)
    all_htmls = []
    for each_markdown in all_markdowns:
        all_htmls.append({"project_content": read_markdown_as_html(each_markdown)})
    return all_htmls


def gen_endnote(content_path):
    """结束语"""
    return read_json_as_map(locate_file(content_path, "foot.json"))


def target_lang():
    return read_text_as_list(locate_file(CONFIG.CURPATH, "cv_lang.txt"))


def gen_default_jump_html(default_html_url):
    output_path = CONFIG.OUTPUT_PATH
    output_index_html_name = CONFIG.OUTPUT_HTML_FOR_DEFAULT_NAME
    with open(locate_file(output_path, output_index_html_name), 'w', encoding="utf8") as wf:
        all_data = { "default_cv_html": default_html_url }
        wf.write(TEMPLATE_ENVIRONMENT.get_template("default.html").render(all_data))


def render_html(target_langs, server_mode=False, local_mode=False, local_company=None, local_jobname=None):
    import copy
    all_data = {}

    index_html_name = CONFIG.OUTPUT_HTML_FOR_DEFAULT_NAME
    print_html_name = CONFIG.OUTPUT_HTML_FOR_PRINT_NAME
    print_pdf_name  = CONFIG.OUTPUT_PDF_FOR_PRINT_NAME

    for each_lang in target_langs:
        lang_dirname = each_lang.lower()
        content_path = locate_file(CONFIG.CONTENT_PATH, lang_dirname)
        output_path = locate_file(CONFIG.OUTPUT_PATH, lang_dirname)

        renew_path(output_path)

        all_data["section"] = get_title_words(CONFIG.TEMPLATE_PATH, lang_dirname)

        all_data["banner"] = gen_banner(content_path, lang_dirname)
        all_data["statements"] = gen_announce(content_path)
        all_data["intro_text"] = gen_introduction(content_path)
        all_data["stack"] = gen_stack(content_path)
        all_data["job"] = gen_job_history(content_path)
        all_data["edu"] = gen_education_history(content_path)
        all_data["cert"] = gen_certification(content_path)
        all_data["projects"] = gen_project_history(content_path)
        all_data["endnote"] = gen_endnote(content_path)

        # 方便打印版本
        with open(locate_file(output_path, print_html_name), 'w', encoding="utf8") as wf:
            all_data_for_print = copy.deepcopy(all_data)

            all_data_for_print["banner"]["myavatar"] = all_data["banner"]["myavatar_offline"]
            all_data_for_print["banner"]["mymobile"] = decode_base64(all_data["banner"]["mymobile_r"])

            if local_mode:
                assert local_jobname is not None
                all_data_for_print["target_jobname"] = local_jobname
                assert local_company is not None
                all_data_for_print["target_company"] = local_company

            if server_mode:
                all_data_for_print["style_prefix"] = CONFIG.CV_URL_ROOT
                all_data_for_print["style_css"] = "mycv-print-online.css"
            else:
                all_data_for_print["style_prefix"] = ".."
                all_data_for_print["style_css"] = "mycv-print.css"

            # 打印版本里精简项目经历
            all_project_counts = len(all_data["projects"])
            masked_project_indexes = set( map( lambda x: all_project_counts - x, ignore_project_item_indexes()) )
            all_data_for_print["projects"] = [ all_data["projects"][i] for i in range(all_project_counts) if i not in masked_project_indexes ]


            wf.write(TEMPLATE_ENVIRONMENT.get_template("print.html").render(all_data_for_print))

        if not local_mode:
            # 在线浏览版本
            with open(locate_file(output_path, index_html_name), 'w', encoding="utf8") as wf:
                all_data["banner"]["myavatar"] = all_data["banner"]["myavatar_online"]
                strwrk = FontElements()
                all_data["banner"]["myname"] = strwrk.convert_string(all_data["banner"]["myname"])
                all_data["banner"]["mymobile"] = strwrk.convert_string(decode_base64(all_data["banner"]["mymobile_m"]))
                all_data["banner"]["myemail"] = strwrk.convert_string(all_data["banner"]["myemail"])
                wf.write(TEMPLATE_ENVIRONMENT.get_template("index.html").render(all_data))

        # 默认显示的简历 URL
        gen_default_jump_html(all_data["banner"]["html_version"])


def gen_pdf(target_langs, server_mode=False):
    """生成PDF文件"""
    print_html_name = CONFIG.OUTPUT_HTML_FOR_PRINT_NAME
    print_pdf_name  = CONFIG.OUTPUT_PDF_FOR_PRINT_NAME

    for each_lang in target_langs:
        lang_dirname = each_lang.lower()
        output_path = locate_file(CONFIG.OUTPUT_PATH, lang_dirname)

        src_html_file = locate_file(output_path, print_html_name)
        des_pdf_file = locate_file(output_path, print_pdf_name)

        html2pdf(src_html_file, des_pdf_file, server_mode)


def bundle_js(theenv):
    js_path = CONFIG.JS_PATH_LOCAL
    renew_path(js_path)

    before_js = Bundle("js/scrollrocket.js",
                       filters="jsmin",
                       output=locate_file(js_path, "mycv-pre.js"))

    theenv.add(before_js)
    before_js.build()


def bundle_css(theenv):
    css_path = CONFIG.CSS_PATH_LOCAL
    renew_path(css_path)

    all_css = Bundle("css/base-min.css",
                     "css/grids-min.css",
                     "css/grids-responsive-min.css",
                     "css/foundation.min.css",
                     "css/jsfid.css",
                     "css/style4.css",
                     "css/mycustom.css",
                     filters="cssmin",
                     output=locate_file(css_path, "mycv-all.css"))

    print_css = Bundle("css/base-min.css",
                       "css/grids-min.css",
                       "css/grids-responsive-min.css",
                       "css/foundation.min.css",
                       "css/print.css",
                       filters="cssmin",
                       output=locate_file(css_path, "mycv-print.css"))

    print_online_css = Bundle("css/base-min.css",
                              "css/grids-min.css",
                              "css/grids-responsive-min.css",
                              "css/foundation.min.css",
                              "css/print-online.css",
                              filters="cssmin",
                              output=locate_file(css_path, "mycv-print-online.css"))
    theenv.register("all_css", all_css)
    all_css.build()
    theenv.register("print_css", print_css)
    print_css.build()
    theenv.register("print_online_css", print_online_css)
    print_online_css.build()


def bundle_all():
    theenv = WebEnv(directory=CONFIG.STATIC_SRC_PATH,
                    url="/static")
    bundle_js(theenv)
    bundle_css(theenv)


def copy_images():
    icon_path = CONFIG.ICO_PATH_LOCAL
    renew_path(icon_path)
    copy_dirs(CONFIG.ICO_SRC_PATH, icon_path)

    img_path = CONFIG.IMG_PATH_LOCAL
    renew_path(img_path)
    copy_dirs(CONFIG.IMG_SRC_PATH, img_path)

