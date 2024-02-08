# 注意事项

1. 本地生成 PDF 格式文档需要系统安装 wkhtmltopdf 。
2. 生成的简历语言版本是通过 `cv_lang.txt` 来控制的(cn 总是放最后)。每次修改语言相关的信息，需要修改:
   1. `cv_lang.txt` 。
   2. `templates/section_titles.json` 。
   3. config.py 。
3. `make local xxxx yyyy` 是仅在本地运行用以生成 PDF 格式文档的命令。
