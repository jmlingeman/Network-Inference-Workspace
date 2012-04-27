default: jar jws
jar:
	jar cvf data.jar \
           6.sif \
           correlations.eda \
           t1db.noa \
           entrez.noa \
           project \
           props \
           vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws: data.jar
	python ../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp



deploy:
	scp props $DEPLOY/../../projects/fly/2005-10/
