PYRUN = $(shell pwd)/ENV/bin/python
ENTRY = $(shell pwd)/run.py
JOB = none
COMPANY = none

clean:
	@test -d output && rm -rf output || true

html: clean
	$(PYRUN) $(ENTRY) --to html

local: clean
	$(PYRUN) $(ENTRY) --to local --company $(COMPANY) --job $(JOB)

pdf: clean
	$(PYRUN) $(ENTRY) --to pdf

all: clean
	$(PYRUN) $(ENTRY) --to all
