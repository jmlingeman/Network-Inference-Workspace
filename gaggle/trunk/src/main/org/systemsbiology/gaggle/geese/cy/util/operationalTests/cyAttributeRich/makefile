default: jar jws

jar:
	jar cvf data.jar \
           immuneResponseFirstNeighbors-0.5.sif \
           confidence.eda \
           intensity.eda \
           heat.noa \
           cold.noa \
           project \
           props \
           vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws: data.jar
	python ../../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp



run:
	java -Xmx1G cytoscape.cytoscape -p runProject
