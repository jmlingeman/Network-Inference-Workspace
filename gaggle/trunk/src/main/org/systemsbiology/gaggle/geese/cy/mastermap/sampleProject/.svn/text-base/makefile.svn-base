jws: jar 0

jwsClean: jar 1

jar:
	jar cvf data.jar \
           project \
           projectClean \
           network.sif \
           pvalues.noa \
           commonNames.noa \
           props \
           vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

0:
	python ../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp

1:
	python ../../../../util/localizeJnlp.py testClean.jnlp-raw > testClean.jnlp
	javaws testClean.jnlp



