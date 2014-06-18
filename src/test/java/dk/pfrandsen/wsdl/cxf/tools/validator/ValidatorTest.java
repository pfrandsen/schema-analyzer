package dk.pfrandsen.wsdl.cxf.tools.validator;

import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.cxf.tools.validator.internal.WSIBPValidator;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;


public class ValidatorTest {
    private AnalysisInformationCollector collector;

    @Before
    public void setUp() {
        collector = new AnalysisInformationCollector();
    }

    @Test
    public void testValid() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/defaultOpMessageNames.wsdl");
        Validator.checkWsdl(wsdlUri, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidateMixedStyle() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/hello_world_mixed_style.wsdl");
        Validator.checkWsdl(wsdlUri, collector);
        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testBasicProfile2716() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/bp2716.wsdl");
        Validator.checkWsdl(wsdlUri, collector);
        assertEquals(7, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testBasicProfile2717and2726() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/bp2717and2726.wsdl");
        //String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/header_rpc_lit.wsdl");
        Validator.checkWsdl(wsdlUri, collector);

        //wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/remove_bp2717and2726.wsdl");
        //Validator.checkWsdl(wsdlUri, collector);

        assertEquals(2, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

        @Test
    public void testBasicProfileRpcLiteral() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/header_rpc_lit.wsdl");
        Validator.checkWsdl(wsdlUri, collector);
        assertEquals(0, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testValidateAttribute() throws Exception {
        String wsdlUri = getLocation("/wsdl/apache-cxf/validator_wsdl/hello_world_error_attribute.wsdl");
        Validator.checkWsdl(wsdlUri, collector);
        assertEquals(1, collector.errorCount());
        assertEquals(0, collector.warningCount());
        assertEquals(0, collector.infoCount());
    }

    @Test
    public void testReflection() {
        int mTrue = 0;
        int mFalse = 0;

        for (Method m : WSIBPValidator.class.getMethods()) {
            System.out.println("Name:" + m.getName() + ", modifiers: " + m.getModifiers());
            boolean pub = Modifier.isPublic(m.getModifiers());
            boolean check = m.getName().startsWith("check");
            boolean modifiers = m.getModifiers() == Member.PUBLIC;
            if (modifiers) {
                mTrue++;
            } else {
                mFalse++;
            }
            boolean combined = check || modifiers;
            System.out.println("Name " + check + " modifiers " + modifiers + " combined " + combined + " public " + pub);
            if (m.getName().startsWith("check") || m.getModifiers() == Member.PUBLIC) {
                System.out.println(m.getName() + " called");
            } else {
                System.out.println(m.getName() + " not called");
            }
        }
        System.out.println(String.format("Modifiers true (%d), false (%d)", mTrue, mFalse));
        System.out.println(String.format("Member.PUBLIC (%d)", Member.PUBLIC));
        System.out.println(String.format("Member.DECLARED (%d)", Member.DECLARED));
    }

    @Test
    public void testPublicMethods() {
        WSIBasicProfileValidatorExtended bp = new WSIBasicProfileValidatorExtended(null);

        bp.isValidChecker();
    }

    protected String getLocation(String wsdlFile) throws URISyntaxException {
        return ValidatorTest.class.getResource(wsdlFile).toURI().getPath();
    }
}
