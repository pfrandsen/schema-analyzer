package upstream.org.apache.cxf.tools.validator.internal;

import javax.wsdl.Definition;
import javax.xml.stream.Location;

import org.apache.cxf.Bus;
import org.apache.cxf.tools.common.ToolContext;
import upstream.org.apache.cxf.tools.validator.AbstractValidator;

public abstract class AbstractDefinitionValidator extends AbstractValidator {

    protected Definition def;
    protected ToolContext env;

    private final Bus bus;

    public AbstractDefinitionValidator() {
        super();
        this.def = null;
        this.bus = null;
    }

    public AbstractDefinitionValidator(final Definition definition) {
        this(definition, null, null);
    }

    public AbstractDefinitionValidator(final Definition definition, final Bus b) {
        this(definition, null, b);
    }

    public AbstractDefinitionValidator(final Definition definition, final ToolContext pEnv, final Bus b) {
        this.def = definition;
        this.env = pEnv;
        this.bus = b;
    }

    public void addError(Location loc, String msg) {
        String errMsg = loc != null ? "line " + loc.getLineNumber() + " of " : "";
        errMsg = errMsg + def.getDocumentBaseURI() + " " + msg;
        addErrorMessage(errMsg);
    }

    public Bus getBus() {
        return this.bus;
    }
}
