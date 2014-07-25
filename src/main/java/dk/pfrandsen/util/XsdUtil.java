package dk.pfrandsen.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class XsdUtil {

    public static String getTargetNamespace(String xsd) throws Exception {
        Path xqLocation = Paths.get("xsd");
        String namespace = XQuery.runXQuery(xqLocation, "targetNamespace.xq", xsd);
        List<String> tns = XQuery.mapResult(namespace, "namespaceUri");
        if (tns.size() >= 1) {
            return tns.get(0);
        }
        return "";
    }

    public static boolean isConcept(String namespace) {
        return namespace.startsWith("http://concept.") || namespace.contains("/concept/");
    }

    public static boolean isInternalNamespace(String namespace) {
        return namespace.startsWith("http://service.schemas.nykreditnet.net/")
                || namespace.startsWith("http://concept.schemas.nykreditnet.net/")
                || namespace.startsWith("http://technical.schemas.nykreditnet.net/")
                || namespace.startsWith("http://process.schemas.nykreditnet.net/");
    }

    public static boolean isInternalServiceNamespace(String namespace) {
        return namespace.startsWith("http://service.schemas.nykreditnet.net/")
                || namespace.startsWith("http://process.schemas.nykreditnet.net/");
    }

    public static boolean isTechnicalNamespace(String namespace) {
        return namespace.startsWith("http://technical.schemas.nykreditnet.net/");
    }

    public static boolean isEnterpriseConcept(String namespace) {
        return namespace.startsWith("http://concept.schemas.nykreditnet.net/");
    }

    public static boolean isServiceConcept(String namespace) {
        return namespace.startsWith("http://service.schemas.nykreditnet.net/") && namespace.contains("/concept/");
    }

    public static boolean isValidTypeName(String name) {
        return Utilities.isUpperCamelCaseAscii(name) && name.endsWith("Type");
    }

    public static boolean isValidElementName(String name) {
        return Utilities.isUpperCamelCaseAscii(name);
    }

    public static boolean isValidEnumerationValue(String value) {
        return value.matches("[a-zA-Z0-9_\\s]+") && !value.matches("[0-9]+.*") && !value.matches("\\s+.*");
    }
}
