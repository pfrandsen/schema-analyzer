package dk.pfrandsen.driver;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.wsdl.WsdlNameChecker;

import java.io.File;

public class SampleDriver {

    // TODO: driver must remove Byte Order Mark from xml if present (else SAX parser will throw exception)

    public static void main(String[] args) {
        AnalysisInformationCollector collector = new AnalysisInformationCollector();
        String path = System.getProperty("user.dir") + "/src/test/resources/wsdl/name/Entity.wsdl";
        System.out.println(path);
        System.out.println(new File(path).toURI());

        String p = "src/test/resources/wsdl/name/Entity.wsdl";
        System.out.println(p);
        System.out.println(new File(p).toURI());

        /* try {
            System.out.println(new File(path).toURI().new File(path).toURI()());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        // String url = "file:///C:/usr/schemaserver/prod_schemas/service/enterprise/customerportfolio/v1/Customerportfolio.wsdl";
        String uri = new File(path).toURI().toString();
        WSDLParser parser = new WSDLParser();
        Definitions definition = parser.parse(uri);
        if (definition != null) {
            System.out.println("Parsed");
            WsdlNameChecker.checkName(definition, collector);
            for (AnalysisInformation err : collector.getErrors()) {
                System.out.println(err.toString());
            }
            for (AnalysisInformation warn : collector.getWarnings()) {
                System.out.println(warn.toString());
            }
            for (AnalysisInformation info : collector.getInfo()) {
                System.out.println(info.toString());
            }
        }
    }
}
