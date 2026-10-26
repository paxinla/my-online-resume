# Pxx's Online Resume (Clojure)

基于 [powerpack](https://github.com/cjohansen/powerpack) 的个人在线简历生成器。
读取 `content/` 下的简历内容，结合 `themes/` 下的主题模板与静态资源，
生成可部署的静态站点（HTML）与 PDF 简历（经本地 wkhtmltopdf）。

## 环境要求

- JDK 17
- Clojure CLI 1.12+
- 本地生成 PDF 需要系统安装 [wkhtmltopdf](https://wkhtmltopdf.org/)；
  也可通过 `config.edn` 的 `:wkhtmltopdf` 指定可执行文件路径，
  未指定时默认从系统 `PATH` 中查找。

## 目录结构

```
deps.edn                    依赖与构建描述（唯一）
config.edn                  全部可配置项（命令行 --config 传入）
Makefile                    用户操作入口
build.clj                   fatjar 构建脚本（clojure -T:build uber）
content/                    简历内容（EDN / Markdown / text）
  cn/…                      中文（源语言，优先级最高）
  en/…                      英文（由 cn 翻译而来）
themes/default-theme/       主题（模板 + 样式 + 脚本 + 图标 + i18n.edn）
  assets-manifest.edn       CSS/JS 打包矩阵声明
resources/schema.edn        powerpack DataScript schema（结构性文件）
src/resume/                 生成器源码
output/                     生成产物（可直接部署为静态站点）
```

## 快速上手

```bash
# 生成在线站点（output/）
make html

# 生成 PDF（含打印页与静态资源）
make pdf

# 生成全部（html + pdf）
make all

# 定制版 PDF：注入应聘公司与职位（对应旧版 make local）
COMPANY=some-company-name JOB=data-engineer make local

# 清理全部中间产物（output/、target/、.cpcache/、resources/static/）
make clean
```

`JOB` / `COMPANY` 未传时默认 `none`；`make local` 会校验两者必须提供。
`make local` 生成的打印版会按 `config.edn` 的 `:project-reduction`
裁剪项目经历（序号从 1 起，对应 `content/<lang>/projects/pN.md`）。

## config.edn

| 键 | 说明 |
|---|---|
| `:content-dir` / `:themes-dir` / `:theme` / `:output-dir` | 各目录相对 `config.edn` 所在目录 |
| `:wkhtmltopdf` | wkhtmltopdf 可执行文件路径；`nil` = 用系统 PATH |
| `:pdf-options` | `:page-size`、`:encoding` 等传给 wkhtmltopdf |
| `:site-title` / `:cv-url-root` / `:github-url` | 站点标题、部署根 URL、GitHub 链接 |
| `:languages` | 语言列表，**顺序即优先级（降序）**：cn 必须第一（唯一源语言，其余语言由它翻译而来）；第一个语言生成根 `index.html` 跳转页 |
| `:output-files` | 输出文件名（在线页 / 打印页 / PDF） |
| `:dev-server` | powerpack 开发服务器端口 |
| `:full-skill-rank` | 技能星级满分（默认 5） |
| `:font-obfuscation` | `:enabled` 开关；`:seed` 为 `nil` 时每次构建随机生成混淆 class，给固定值便于调试 |
| `:project-reduction` | 打印版裁剪的项目序号列表 |

## 生成流程

1. **资产**（`resume.assets`）：按 `themes/default-theme/assets-manifest.edn`
   把 CSS/JS 打包为 `output/static/{css,js}`（含构建期字形混淆 CSS 注入、
   rem→px 回退注入、`@@CV-URL-ROOT@@` 占位符替换），并复制 `icons/`、`img/`。
2. **HTML**（powerpack export）：页面由 `:powerpack/on-ingested` 回调依
   `config.edn` 注册——根跳转页 `/`、每语言 `/<lang>/`（在线版）与
   `/<lang>/print.html`（打印版）。
3. **PDF**（`resume.pdf`）：`ProcessBuilder` 调用 wkhtmltopdf 把
   `print.html` 转为 PDF（`--quiet --encoding UTF-8 --page-size A4
   --enable-local-file-access`）。

`--server` 标志（fatjar 的 `run-jar` 目标已带）用于在部署服务器上
出 PDF：打印页改用绝对 URL 引用 `mycv-print-online.css`。

## 主题（themes）

新增主题只需复制 `default-theme` 改名并修改 `config.edn` 的 `:theme`。
主题目录约定：

```
templates/        Selmer 模板（base/index/print/default + 各 section 分片）
i18n.edn          各语言界面文案
assets-manifest.edn  打包矩阵（bundle 输入顺序、占位符替换、复制目录）
css/ js/ icons/ img/  源资产
```

CSS 源要求 minification-safe（无行注释、语句带分号）；`rem` 值会在构建期
自动注入 px 回退行（IE8 兼容）；`#fontmap#` 哨兵会被替换为本次构建的
字形混淆 CSS。

## 防拷贝

- 在线版的姓名/手机/邮箱逐字符替换为空 `<span class>`，字形由构建期生成
  的 CSS `content` 伪元素注入；class 后缀每次构建随机（可 `:seed` 固定）。
- `mycv-pre.js` 阻断右键/选中/复制/剪切/拖拽/常用快捷键，并破解 iframe 嵌套。
- 打印版（`print.html`）不受混淆影响，使用真实信息；打印样式永不进入在线页。
- 全部 JS 为 ES5（IE8+ 兼容）；禁用 JS 时页面仍可正常阅读。

## 浏览器兼容

主题面向 IE8+ 设计：XHTML 1.0 Transitional doctype、Pure 静态栅格、
`display:table` 布局（工作经历年份/详情两列定宽对齐）、无 CSS 变量、
rem 带 px 回退、CSS3 渐变/圆角/阴影仅作渐进增强。

## 开发模式

```bash
make dev
```

启动后会在标准输出打印各语言预览地址（默认语言 cn 在前），例如：
`http://localhost:4242/cn/`。

powerpack 开发服务器（默认端口取 `:dev-server`）带源码热重载；
静态资产以 live 模式经 optimus 提供，主题/内容改动刷新即见。

## fatjar 与离网部署

```bash
make jar        # → target/online-resume-standalone-1.0.0.jar
make run-jar    # 等价: java -jar target/…jar --to all --server --config config.edn
```

离网服务器要求：JDK 8+、本仓库完整目录（content/themes/resources/config.edn
随仓库携带）、已安装 wkhtmltopdf（路径可写入 config.edn）。
在项目根目录执行：

```bash
java -jar target/online-resume-standalone-1.0.0.jar --to all --server --config config.edn
```

生成完成后将 `output/` 整目录部署到静态 Web 服务器即可（与旧实现产物同构）。
