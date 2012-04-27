jws0: jar 0
jws1: jar 1
jws2: jar 2

jar:
	jar cvf data.jar \
           project0 \
           project1 \
           project2 \
           halo3.sif \
           halo46.sif \
           props \
           vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

0:
	python ../../../../util/localizeJnlp.py test0.jnlp-raw > test0.jnlp
	javaws test0.jnlp

1:
	python ../../../../util/localizeJnlp.py test1.jnlp-raw > test1.jnlp
	javaws test1.jnlp

2:
	python ../../../../util/localizeJnlp.py test2.jnlp-raw > test2.jnlp
	javaws test2.jnlp


