PYRUN = $(shell pwd)/ENV/bin/python
ENTRY = $(shell pwd)/run.py
JOB = none
COMPANY = none

html:
	$(PYRUN) $(ENTRY) --to html

local:
	$(PYRUN) $(ENTRY) --to local --company $(COMPANY) --job $(JOB)

pdf:
	$(PYRUN) $(ENTRY) --to pdf

all:
	$(PYRUN) $(ENTRY) --to all
