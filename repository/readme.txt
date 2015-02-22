Local Maven repository for the jars used by this tool that are not in global Maven repositories.

The "source" jars are located in the projects lib folder.

The jars are installed in this local repository using these commands:

mvn install:install-file -Dfile=../lib/mxquery.jar -DgroupId=ch.ethz -DartifactId=mxquery -Dversion=local -Dpackaging=jar -DlocalRepositoryPath=.
mvn install:install-file -Dfile=../lib/wsi-checker-1.1.1.jar -DgroupId=pfrandsen -DartifactId=wsi-checker -Dversion=1.1.1 -Dpackaging=jar -DlocalRepositoryPath=.
