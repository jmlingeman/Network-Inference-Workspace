default: data.jar jws

data.jar:
	jar cvf data.jar \
          Column.noa \
          Flag_F.noa \
          Flag_N.noa \
          INTERRUPTED_BY.noa \
          ISH_ELEMENT.noa \
          Orientation.noa \
          Plate.noa \
          Row.noa \
          Source1.noa \
          Source2.noa \
          Start.noa \
          Stop.noa \
          commonName.noa \
          nodeType.noa \
          primer_forward_name.noa \
          primer_forward_sequence.noa \
          primer_reverse_name.noa \
          primer_reverse_sequence.noa \
          replicon.noa \
          weights.eda \
          width.noa \
          net540.gml \
          project \
          props \
          vizmap.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker data.jar  gaggle

jws: data.jar
	python ../../../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp


