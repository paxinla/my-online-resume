#!/usr/bin/env python
#coding=utf-8


import sys
import argparse
import platform

from config import CONFIG
from rule import target_lang, render_html, gen_pdf, bundle_all, copy_images


def parse_input():
    parser = argparse.ArgumentParser(description="Generate personal resume files.")

    parser.add_argument("--to", dest="gen_scope", type=str, required=True,
                        choices=("all", "html", "pdf", "local"),
                        help="html for pages ; pdf for only pdf file ; all for both ; local for local pdf file.")
    parser.add_argument("--company", dest="local_company_name", type=str, required=False,
                        help="company name for local pdf file only.")
    parser.add_argument("--job", dest="local_jobname", type=str, required=False,
                        help="job name for local pdf file only.")
    parser.add_argument("--server", dest="server_mode", action="store_true",
                        help="server mode run in a remote server.")
    parser.set_defaults(server_mode=False)

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(0)

    return vars(parser.parse_args())


def main():
    in_args = parse_input()

    copy_images()
    bundle_all()
    target_langs = target_lang()
    target_filetype = in_args["gen_scope"]
    server_mode = in_args["server_mode"]

    if target_filetype == "html":
        render_html(target_langs)
    elif target_filetype == "pdf":
        gen_pdf(target_langs)
    elif target_filetype == "local":
        if "local_jobname" in in_args and in_args["local_jobname"] is not None and in_args["local_jobname"].strip() != '':
            local_job_name = in_args["local_jobname"]
        else:
            local_job_name = None

        if "local_company_name" in in_args and in_args["local_company_name"] is not None and in_args["local_company_name"].strip() != '':
            local_company_name = in_args["local_company_name"]
        else:
            local_company_name = None


        render_html(target_langs, False, True, local_company_name, local_job_name)
        if platform.system() == "Windows":
            gen_pdf(target_langs, True)
        else:
            gen_pdf(target_langs, False)
    elif target_filetype == "all":
        render_html(target_langs, server_mode)
        gen_pdf(target_langs, server_mode)
    else:
        raise ValueError(f"Unsuppored target {target_filetype} !!!")


if __name__ == "__main__":
    main()






