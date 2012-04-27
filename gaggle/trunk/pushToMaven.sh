#!/bin/sh
su dtenenba -c "mvn deploy:deploy-file -DgroupId=org.systemsbiology.gaggle -DartifactId=gaggle-core -Dversion=1.0 -Dpackaging=jar -Dfile=dist/gaggle-core.jar -DrepositoryId=organizational -Durl=scp://dtenenba@atlas//local/wwwspecial/gaggle/maven_repository"

