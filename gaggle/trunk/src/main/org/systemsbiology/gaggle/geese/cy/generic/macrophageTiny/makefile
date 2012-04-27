default: jar sign

jar:
	jar cvf data.jar \
           commonNames.noa \
           CoreNet.gml \
           CoreNet.sif \
           cy.jnlp \
           friendlyName.noa \
           locuslink.noa \
           jax.noa \
           makefile \
           moleculeType.noa \
           project \
           props \
           vizmap.props 

sign:
	jarsigner -keystore   /users/pshannon/.jarkey -storepass honker data.jar gaggle


jws: jar sign
	python ../../../../util/localizeJnlp.py cy.jnlp-raw > cy.jnlp
	javaws cy.jnlp

