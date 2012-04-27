copy:
	python copyToHazel.py

cleanHazel:
	find . -name ".svn" -exec rm -rf {} ';'

build:
	R CMD check gaggleUtilTest
	R CMD build gaggleUtilTest

