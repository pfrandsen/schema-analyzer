package dk.pfrandsen.wsdl.util;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;

// TODO: Determine if this should be rewritten to use the CXF WSDL parser classes

public class WSDLBindingProperties {
    /**
     * @param url URL to wsdl file
     * @return Array with two strings, first is binding name and second is binding namespace
     * @throws BadDataException Thrown if wsdl does not contain exactly one binding
     */
    public String[] getBindingProperties(String url) throws BadDataException {
        WSDLParser parser = new WSDLParser();
        return getBindingProperties(parser.parse(url));
    }

    /**
     * @param definitions Parsed wsdl
     * @return Array with two strings, first is binding name and second is binding namespace
     * @throws BadDataException Thrown if wsdl does not contain exactly one binding
     */
    public String[] getBindingProperties(Definitions definitions) throws BadDataException {
        String[] properties = {"", ""};
        if (definitions.getBindings().size() != 1) {
            throw new BadDataException("There should be exactly one binding - found " + definitions.getBindings().size());
        }
        Binding binding = definitions.getBindings().get(0);
        properties[0] = binding.getName();
        properties[1] = binding.getType().getNamespaceURI();
        return properties;
    }

    public static void main(String[] args) {
        WSDLBindingProperties config = new WSDLBindingProperties();
        try {
            String[] props = config.getBindingProperties(
                    "http://service.schemas.nykreditnet.net/external/gjensidige/forsikringskunde/v1/Forsikringskunde.wsdl");
            System.out.println("Name: '" + props[0] + "'");
            System.out.println("Namespace: '" + props[1] + "'");
        } catch (BadDataException e) {
            e.printStackTrace();
        }
    }
}
