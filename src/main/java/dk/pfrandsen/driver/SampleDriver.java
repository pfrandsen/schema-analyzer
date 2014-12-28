package dk.pfrandsen.driver;

import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import dk.pfrandsen.wsdl.WsdlNameChecker;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;

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

        try {
            String wsdl = IOUtils.toString(new FileInputStream(path));
            System.out.println("Read");
            WsdlNameChecker.checkName(wsdl, collector);
            for (AnalysisInformation err : collector.getErrors()) {
                System.out.println(err.toString());
            }
            for (AnalysisInformation warn : collector.getWarnings()) {
                System.out.println(warn.toString());
            }
            for (AnalysisInformation info : collector.getInfo()) {
                System.out.println(info.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
