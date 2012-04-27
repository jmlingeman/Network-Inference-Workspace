default: data.jar jws

data.jar:
	jar cvf data.jar \
          confidence.eda \
          makefile \
          net30.sif \
          pValues.eda \
          project \
          props \
          vizmap.props

	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws:
	python ../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp

