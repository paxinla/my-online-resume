#!/usr/bin/env python
#coding=utf-8


import os

import shutil
import simplejson as json
import markdown
import pdfkit
import platform
import base64

from config import CONFIG


def locate_file(path_part, filename_part):
    return os.path.join(path_part, filename_part)

def join_path(path_list):
    return os.sep.join(path_list)


def renew_path(filepath):
    if os.path.exists(filepath):
        shutil.rmtree(filepath)

    os.makedirs(filepath)


def copy_dirs(srcpath, despath):
    if os.path.exists(despath):
        shutil.rmtree(despath)

    shutil.copytree(srcpath, despath)


def decode_base64(base64_str):
    return base64.b64decode(base64_str.encode("utf-8")).decode()


def read_json_as_map(json_filepath):
    rs = {}

    if not os.path.exists(json_filepath):
        raise RuntimeError(f"Expected source json file {json_filepath} not found!")

    with open(json_filepath, 'r', encoding="utf8") as rf:
        rs = json.loads(rf.read())

    return rs


def read_markdown_as_html(mkd_filepath):
    rs = ''

    if not os.path.exists(mkd_filepath):
        raise RuntimeError(f"Expected source markdown file {mkd_filepath} not found!")

    with open(mkd_filepath, 'r', encoding="utf8") as rf:
        rs = markdown.markdown(rf.read())

    return rs


def read_text_as_list(text_filepath):
    rs = []

    if not os.path.exists(text_filepath):
        raise RuntimeError(f"Expected source text file {text_filepath} not found!")

    with open(text_filepath, 'r', encoding="utf8") as rf:
        for each_line in rf:
            rs.append(each_line.strip())

    return rs


def html2pdf(src_html_file, des_pdf_file, server_mode):
    if os.path.exists(des_pdf_file):
        os.remove(des_pdf_file)

    if not os.path.exists(src_html_file):
        raise RuntimeError(f"Expected source file {src_html_file} not found!")

    options = {
        "quiet": "",
        "encoding": "UTF-8",
        "page-size": "A4",
        'enable-local-file-access': None
    }

    if platform.system() == "Windows":
        configuration = pdfkit.configuration(wkhtmltopdf=CONFIG.path_wkthmltopdf)
        pdfkit.from_file(src_html_file, des_pdf_file, options=options, configuration=configuration)
    else:
        if server_mode:
            server_configuration = pdfkit.configuration(wkhtmltopdf="/usr/bin/wkhtmltopdf")
            pdfkit.from_file(src_html_file, des_pdf_file, options=options, configuration=server_configuration, css=locate_file(CONFIG.CSS_PATH_LOCAL, "mycv-print-online.css"))
        else:
            pdfkit.from_file(src_html_file, des_pdf_file, options=options)


def list_markdown_files(dirpath):
    if not os.path.exists(dirpath):
        raise RuntimeError(f"Expected source directory {dirpath} not found!")

    if not os.path.isdir(dirpath):
        raise ValueError(f"Expected source {dirpath} is not a directory!")

    rs = []
    for root, _, files in os.walk(dirpath):
        for each_file in files:
            if each_file.endswith("md") or each_file.endswith("mkd") or each_file.endswith("markdown"):
                rs.append(os.path.join(root, each_file))

    return rs
