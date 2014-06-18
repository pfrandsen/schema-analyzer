package dk.pfrandsen.wsdl.util;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.factory.WSDLFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Wsdl {

    public static Definition getWsdlDefinition(String uri) throws WSDLException {
        WSDLReader reader = WSDLFactoryImpl.newInstance().newWSDLReader();
        reader.setFeature(Constants.FEATURE_VERBOSE, false);
        reader.setFeature("javax.wsdl.importDocuments", true);
        Definition definition = reader.readWSDL(uri);
        return definition;
    }

    public static List<Binding> getBindings(Definition definition) {
        List<Binding> result = new ArrayList<>();
        Map bindings = definition.getBindings();
        for (Object item : bindings.entrySet()) {
            Map.Entry entry = (Map.Entry) item;
            if (entry.getValue() instanceof Binding) {
                result.add((Binding) entry.getValue());
            }
        }
        return result;
    }

    public static List<Service> getServices(Definition definition) {
        List<Service> result = new ArrayList<>();
        Map services = definition.getServices();
        for (Object item : services.entrySet()) {
            Map.Entry entry = (Map.Entry) item;
            if (entry.getValue() instanceof Service) {
                result.add((Service) entry.getValue());
            }
        }
        return result;
    }

}
