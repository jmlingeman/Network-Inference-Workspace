help:
	egrep "^#" makefile | sed "s/^#//"


# --- view
# deleted all derived files, and creates a fresh pdf
#

view: clean pdf

# --- dvi
# displays the dvi version of the vignette
#
dvi: gaggle.dvi
	xdvi gaggle.dvi

gaggle.dvi: gaggle.tex
	latex gaggle.tex

# --- gaggle.tex
# makes a fresh .tex file from the .Rnw file
#
gaggle.tex: gaggle.Rnw
	echo 'Sweave ("gaggle.Rnw")' | R --vanilla --quiet

# --- pdf
# makes a fresh pdf file
#
pdf: gaggle.tex
	texi2dvi --pdf gaggle.tex
	sleep 3
	open gaggle.pdf
	rm gaggle.aux gaggle.log gaggle.out gaggle.tex


# --- clean
# removes all intermediate and derived files, but not pdf
#
clean:
	- rm *.dvi *.log *.tex *.aux *.out

# --- fullclean
# removes all intermediate and derived files, including pdf
#
fullclean: clean
	- rm *.pdf
