
Note:

Do not add wsi-checker-1.0-SNAPSHOT.jar to classpath - it contains an old version of wsdl4j that is not
compatible with the code in this project. This older version of wsdl4j is needed by the WS-I WSDL validation
tool (included in the jar) and this tool does _not_ work with newer versions of wsdl4j.
