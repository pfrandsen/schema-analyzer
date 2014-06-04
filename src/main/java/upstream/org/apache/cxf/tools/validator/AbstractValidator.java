package upstream.org.apache.cxf.tools.validator;

import java.util.List;
import java.util.Vector;

public abstract class AbstractValidator {
    protected List<String> errorMessages = new Vector<String>();

    public AbstractValidator() {
    }

    public abstract boolean isValid();

    public void addErrorMessage(String err) {
        errorMessages.add(err);
    }

    public String getErrorMessage() {
        StringBuilder strbuffer = new StringBuilder();
        for (int i = 0; i < errorMessages.size(); i++) {
            strbuffer.append(errorMessages.get(i));
            strbuffer.append(System.getProperty("line.separator"));
        }
        return strbuffer.toString();
    }
}
