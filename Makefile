CLOJURE := /opt/clojure/bin/clojure
JAVA    := /opt/jdk-17/bin/java
# ?= ：环境式（COMPANY=x make local）与参数式（make local COMPANY=x）均可覆盖
JOB     ?= none
COMPANY ?= none

.PHONY: clean html local pdf all jar run-jar dev help

help:
	@echo "COMPANY=<公司> JOB=<职位> make local   # 生成定制 PDF（本地）"
	@echo "make html                              # 生成在线站点（HTML）"
	@echo "make pdf                               # 生成 PDF（含打印页）"
	@echo "make all                               # html + pdf"
	@echo "make jar                               # 打包 fatjar"
	@echo "make run-jar                           # 用 fatjar 生成全部产物"
	@echo "make dev                               # 启动 powerpack 开发服务器"
	@echo "make clean                             # 清理全部中间产物"

clean-output:
	@test -d output && rm -rf output || true
	@echo "cleaned: output/"

clean-jar:
	@test -d target && rm -rf target .cpcache resources/static .nrepl-port || true
	@echo "cleaned: output/ target/ .cpcache/ resources/static/"

html: clean-output
	$(CLOJURE) -M:run -- --to html --config config.edn

local: clean-output
	$(CLOJURE) -M:run -- --to local --company "$(COMPANY)" --job "$(JOB)" --config config.edn

pdf: html
	$(CLOJURE) -M:run -- --to pdf --config config.edn

all: clean-output clean-jar
	$(CLOJURE) -M:run -- --to all --config config.edn

jar: clean-jar
	$(CLOJURE) -T:build uber

run-jar: jar
	$(JAVA) -jar target/online-resume-standalone-1.0.0.jar --to all --server --config config.edn

dev: clean-output clean-jar
	$(CLOJURE) -M:dev:run -- dev --config config.edn

