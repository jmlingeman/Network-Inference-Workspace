default: data.jar jws

data.jar:
	jar cvf data.jar \
          tiny.sif \
          logicGateNodeNames.noa \
          types.noa \
          clusterGeneCount.noa \
          clusterConditionCount.noa \
          clusterGenes.noa \
          clusterConditions.noa \
          clusterResiduals.noa \
          clusterMotifPValues.noa \
          weights.eda \
          props \
          vizmap.props \
          project

	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws:
	python ../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp

