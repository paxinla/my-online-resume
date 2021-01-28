#!/usr/bin/env python
#coding=utf-8


import os


class CONFIG(object):
    CURPATH = os.path.dirname(os.path.abspath(__file__))

    TEMPLATE_PATH = os.path.join(CURPATH, "templates")
    CONTENT_PATH = os.path.join(CURPATH, "content")

    STATIC_SRC_PATH = os.path.join(CURPATH, "static")
    JS_SRC_PATH = os.path.join(STATIC_SRC_PATH, "js")
    CSS_SRC_PATH = os.path.join(STATIC_SRC_PATH, "css")
    IMG_SRC_PATH = os.path.join(STATIC_SRC_PATH, "img")
    ICO_SRC_PATH = os.path.join(STATIC_SRC_PATH, "icons")

    OUTPUT_PATH = os.path.join(CURPATH, "output")
    STATIC_PATH = os.path.join(OUTPUT_PATH, "static")

    CV_URL_ROOT = "https://paxinla.github.io/my-online-resume"

    JS_PATH_LOCAL = os.path.join(STATIC_PATH, "js")
    CSS_PATH_LOCAL = os.path.join(STATIC_PATH, "css")
    IMG_PATH_LOCAL = os.path.join(STATIC_PATH, "img")
    IMG_PATH_GITHUB = CV_URL_ROOT + "/static/img"
    ICO_PATH_LOCAL = os.path.join(STATIC_PATH, "icons")

    GITHUB_URL = "https://paxinla.github.io"
    VERSION_URL = {"cn": {"HTML_VERSION_URL": GITHUB_URL + "/my-online-resume/cn/",
                          "PDF_VERSION_URL": CV_URL_ROOT + "/cn/cv_charles.pdf"},
                   "en": {"HTML_VERSION_URL": GITHUB_URL + "/my-online-resume/en/",
                          "PDF_VERSION_URL": CV_URL_ROOT + "/en/cv_charles.pdf"}}

    path_wkthmltopdf = r"D:\Program Files\wkhtmltopdf\bin\wkhtmltopdf.exe"

    FULL_SKILL_RANK = 5
