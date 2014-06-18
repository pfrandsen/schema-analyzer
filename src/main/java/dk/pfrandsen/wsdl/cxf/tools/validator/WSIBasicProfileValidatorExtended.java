package dk.pfrandsen.wsdl.cxf.tools.validator;

import javax.wsdl.Definition;

public class WSIBasicProfileValidatorExtended extends WSIBasicProfileValidator {

    public WSIBasicProfileValidatorExtended(Definition def) {
        super(def);
    }

    public boolean checkBindingExtended() {
        return true;
    }
}
