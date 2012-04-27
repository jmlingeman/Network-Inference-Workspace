help:
	egrep "^#" makefile | sed "s/^#//"

# --- copy
# use a python script to copy crucial parts of this R package to a linux box, for building
# vignettes directory is deleted

copy:
	python copyToHazel.py

# --- copyVig
# use a python script to copy crucial parts of this R package to a linux box, for building
# vignettes directory is preserved

copyVig:
	python copyToHazel.py -vig

# --- check
# to be run on linux box:  check the package in all the ways R typically does. takes
# several minutes
check:
	R CMD check gaggle; rm -rf gaggle.Rcheck

# --- build
# to be run on linux box:  build the package
build:
	R --no-init-file CMD build gaggle

# --- install
# 
install:
	R CMD install gaggle_*.tar.gz

# --- pdf
# generate a fresh copy of the vignette, gaggle.pdf
pdf:
	(cd gaggle/inst/doc; make pdf)


# --- repos
# update the repository on db so that it knows about the latest version of packages
# installed there

repos: 
	scp gaggle_*.tar.gz db:/net/dblocal/wwwspecial/gaggle/R/packages/gaggle/1.0/src/contrib
	ssh db 'R --no-save < /net/dblocal/wwwspecial/gaggle/R/packages/gaggle/1.0/rebuild.R'
	
# --- docs
# copy the latest installed gaggle html docs from trickster to db, then recreate
# the index web page for them

DOC_SRC=/Library/Frameworks/R.framework/Versions/Current/Resources/library/gaggle
#DOC_DEST=/Volumes/DBlocal/wwwspecial/gaggle/docs/html/r/cribsheet/
DOC_DEST=/Volumes/DBlocal/wwwspecial/gaggle/docs/geese/r/cribsheet/

docs: 
	cp $(DOC_SRC)/html/*.html $(DOC_DEST)/gagglePkg.autogen/
#	cp $(DOC_SRC)/doc/*.pdf $(DOC_DEST)/vignettes/
	cp $(DOC_SRC)/doc/*.png $(DOC_DEST)/vignettes/
	#ssh dtenenba@db '(cd $(DOC_DEST) python updateIndex.py)'
	pushd $(DOC_DEST);python updateIndex.py;popd
	
