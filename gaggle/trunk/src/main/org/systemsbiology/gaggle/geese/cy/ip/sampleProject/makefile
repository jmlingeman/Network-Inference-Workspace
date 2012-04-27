default: data.jar jws

data.jar:
	jar cvf data.jar \
           network.sif \
           peptides.eda \
           probabilities.eda \
           project \
           props \
           vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws:
	javaws test.jnlp






