package dk.pfrandsen.wsdl;

import java.util.ArrayList;
import java.util.List;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Service;

public class ServiceChecker {
    public static String SERVICE_PORT_POSTFIX = "WS";

    public static String removeVersion(String serviceName) {
        if (serviceName.matches(".+V[0-9]+")) {
            int index = serviceName.lastIndexOf('V');
            return serviceName.substring(0, index);
        }
        return serviceName;
    }

    public static List<String> getServiceNames(Definitions definitions, boolean removeVersion) {
        List<String> serviceNames = new ArrayList<String>();
        for (Service service : definitions.getLocalServices()) {
            String name = service.getName();
            if (removeVersion) {
                name = removeVersion(name);
            }
            serviceNames.add(name);
        }
        return serviceNames;
    }
}