package dk.pfrandsen.wsdl.cxf;

import org.apache.cxf.tools.common.ToolTestBase;
import org.apache.cxf.tools.validator.WSDLValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.Assert.assertTrue;

public class WSDLValidationTest extends ToolTestBase {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testValidateDefaultOpMessageNames() throws Exception {
        String[] args = new String[] {"-verbose",
                getLocation("/wsdl/apache-cxf/validator_wsdl/defaultOpMessageNames.wsdl")};
        WSDLValidator.main(args);
        System.out.println(this.getStdOut());
        assertTrue(this.getStdOut().contains("Valid WSDL"));
    }

    @Override
    protected String getLocation(String wsdlFile) throws Exception {
        Enumeration<URL> e = WSDLValidationTest.class.getClassLoader().getResources(wsdlFile);
        while (e.hasMoreElements()) {
            URL u = e.nextElement();
            File f = new File(u.toURI());
            if (f.exists() && f.isDirectory()) {
                return f.toString();
            }
        }

        return WSDLValidationTest.class.getResource(wsdlFile).toURI().getPath();
    }

}
