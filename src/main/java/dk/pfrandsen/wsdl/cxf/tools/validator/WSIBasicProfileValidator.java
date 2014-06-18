package dk.pfrandsen.wsdl.cxf.tools.validator;

import org.apache.cxf.tools.common.ToolException;
import upstream.org.apache.cxf.tools.validator.internal.WSIBPValidator;

import javax.wsdl.Definition;
import java.lang.reflect.Method;

public class WSIBasicProfileValidator extends WSIBPValidator {

    public WSIBasicProfileValidator(Definition def) {
        super(def);
    }

    public boolean isValid() {
        boolean valid = true;
        for (Method m : getClass().getMethods()) {
            if (m.getName().startsWith("check")) {
                if (m.getGenericReturnType() == boolean.class && m.getGenericParameterTypes().length == 0) {
                    try {
                        Boolean res = (Boolean)m.invoke(this);
                        if (!res) {
                            valid = false;
                        }
                    } catch (Exception e) {
                        throw new ToolException(e);
                    }
                }
            }
        }
        return valid;
    }

    public void isValidChecker() {
        System.out.println(getClass().getName());
        for (Method m : getClass().getMethods()) {
            if (m.getName().startsWith("check")) {
                if (m.getGenericReturnType() == boolean.class && m.getGenericParameterTypes().length == 0) {
                    System.out.println(m.getName());
                }
            }
        }
    }
}
