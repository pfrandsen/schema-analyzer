package upstream.org.apache.cxf.tools.validator.internal;

import javax.wsdl.Definition;
import javax.xml.stream.Location;

import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.validator.internal.ValidationResult;
import upstream.org.apache.cxf.tools.validator.AbstractValidator;

public abstract class AbstractDefinitionValidator extends AbstractValidator {

    protected Definition def;
    protected ToolContext env;
    private ValidationResult vResults = new ValidationResult();
    private boolean suppressWarnings;

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

    public void setSuppressWarnings(boolean s) {
        this.suppressWarnings = s;
    }

    public ValidationResult getValidationResults() {
        return this.vResults;
    }

    public void addErrorMessage(String err) {
        super.addErrorMessage(err);
        addError(err);
    }

    public void addError(Location loc, String msg) {
        String errMsg = loc != null ? "line " + loc.getLineNumber() + " of " : "";
        errMsg = errMsg + def.getDocumentBaseURI() + " " + msg;
        addErrorMessage(errMsg);
    }

    protected void addError(final Message msg) {
        vResults.addError(msg.toString());
    }

    protected void addError(final String error) {
        vResults.addError(error);
    }

    protected void addWarning(final Message msg) {
        if (!suppressWarnings) {
            vResults.addWarning(msg);
        }
    }

    protected void addWarning(final String warning) {
        if (!suppressWarnings) {
            vResults.addWarning(warning);
        }
    }

    protected boolean isSuccessful() {
        return vResults.isSuccessful();
    }

    public Bus getBus() {
        return this.bus;
    }
}
