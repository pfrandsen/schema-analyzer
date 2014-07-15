package dk.pfrandsen.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class XsdUtil {

    public static String getTargetNamespace(String wsdl) throws Exception {
        Path xqLocation = Paths.get("xsd");
        String namespace = XQuery.runXQuery(xqLocation, "targetNamespace.xq", wsdl);
        List<String> tns = XQuery.mapResult(namespace, "namespaceUri");
        if (tns.size() >= 1) {
            return tns.get(0);
        }
        return "";
    }

}
