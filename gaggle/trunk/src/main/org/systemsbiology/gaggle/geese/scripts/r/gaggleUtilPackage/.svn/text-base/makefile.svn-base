help:
	egrep "^#" makefile | sed "s/^#//"

# --- copy
# use a python script to copy crucial parts of this R package to a linux box, for building
copy:
	python copyToHazel.py

# --- check
# to be run on linux box:  check the package in all the ways R typically does. takes
# several minutes
check:
	R CMD check gaggleUtil

# --- build
# to be run on linux box:  build the package
build:
	R CMD build gaggleUtil


# --- repos
# update the repository on db so that it knows about the latest version of packages
# installed there

repos: 
	scp gaggleUtil_*.tar.gz db:/net/dblocal/wwwspecial/gaggle/R/packages/gaggle/1.0/src/contrib
	ssh db 'R --no-save < /net/dblocal/wwwspecial/gaggle/R/packages/gaggle/1.0/rebuild.R'
	
# --- docs
# copy the latest installed gaggle html docs from trickster to db, then recreate
# the index web page for them

DOC_SRC=/Library/Frameworks/R.framework/Versions/2.3/Resources/library/gaggleUtil/html
DOC_DEST=/net/dblocal/wwwspecial/gaggle/docs/html/r/cribsheet

docs: 
	scp $(DOC_SRC)/*.html db:$(DOC_DEST)/gaggleUtilPkg.autogen
	ssh db '(cd $(DOC_DEST); python updateIndex.py)'
	
