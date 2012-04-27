copy:
	python copyToHazel.py

cleanHazel:
	find . -name ".svn" -exec rm -rf {} ';'

build:
	R CMD check gaggleGo
	R CMD build gaggleGo

