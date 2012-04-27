default:
	(cd network; make)
	(cd experiment; make)
	(cd util; make)
	(cd boss; make)
	(cd geese; make)

doc:
	javadoc \
                   -source 1.5 \
                   -d ./htdocs\
                   -private \
                   -sourcepath ../../.. \
                   org.systemsbiology.gaggle.boss \
                   org.systemsbiology.gaggle.boss.plugins \
                   org.systemsbiology.gaggle.experiment \
                   org.systemsbiology.gaggle.experiment.datamatrix \
                   org.systemsbiology.gaggle.experiment.metadata \
                   org.systemsbiology.gaggle.experiment.readers \
                   org.systemsbiology.gaggle.experiment.repository \
                   org.systemsbiology.gaggle.geese \
                   org.systemsbiology.gaggle.network \
                   org.systemsbiology.gaggle.util \
               -windowtitle gaggle \
       -doctitle "gaggle"

deployDoc:
	scp -r htdocs/* db:/net/dblocal/wwwspecial/gaggle/docs/html/javadoc/
   

