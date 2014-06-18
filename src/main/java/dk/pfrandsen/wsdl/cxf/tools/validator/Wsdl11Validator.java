package dk.pfrandsen.wsdl.cxf.tools.validator;

import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.cxf.Bus;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.catalog.OASISCatalogManagerHelper;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.util.URIParserUtil;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.validator.internal.SchemaValidator;
import org.apache.cxf.tools.validator.internal.ValidationResult;
//import upstream.org.apache.cxf.tools.validator.AbstractValidator;
import upstream.org.apache.cxf.tools.validator.AbstractValidator;
import upstream.org.apache.cxf.tools.validator.internal.*;
import org.apache.cxf.wsdl.WSDLManager;
import org.w3c.dom.Document;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Wsdl11Validator extends WSDL11Validator {
    public static String ASSERTION_ID = "CXF-WSDL-Validation";
    private final List<AbstractDefinitionValidator> validators = new ArrayList<AbstractDefinitionValidator>();

    public Wsdl11Validator(Definition definition) {
        super(definition);
    }

    public Wsdl11Validator(Definition definition, ToolContext pe) {
        super(definition, pe);
    }

    public Wsdl11Validator(Definition definition, ToolContext pe, Bus b) {
        super(definition, pe, b);
    }

    public boolean isValid() throws ToolException {
        boolean isValid = true;
        String schemaDir = getSchemaDir();
        SchemaValidator schemaValidator = null;
        String[] schemas = (String[])env.get(ToolConstants.CFG_SCHEMA_URL);
        // Tool will use the following sequence to find the schema files
        // 1.ToolConstants.CFG_SCHEMA_DIR from ToolContext
        // 2.ToolConstants.CXF_SCHEMA_DIR from System property
        // 3.If 1 and 2 is null , then load these schema files from jar file
        String wsdl = (String)env.get(ToolConstants.CFG_WSDLURL);

        Document doc = getWSDLDoc(wsdl);
        if (doc == null) {
            return true;
        }
        if (this.def == null) {
            try {
                this.def = getBus().getExtension(WSDLManager.class).getDefinition(wsdl);
            } catch (WSDLException e) {
                throw new ToolException(e);
            }
        }

        WSDLRefValidator wsdlRefValidator = new WSDLRefValidator(this.def, doc, getBus());
        wsdlRefValidator.setSuppressWarnings(env.optionSet(ToolConstants.CFG_SUPPRESS_WARNINGS));
        validators.add(wsdlRefValidator);

        if (env.fullValidateWSDL()) {
            validators.add(new UniqueBodyPartsValidator(this.def));
            validators.add(new WSIBasicProfileValidator(this.def));
            validators.add(new MIMEBindingValidator(this.def));
        }

        for (AbstractValidator validator : validators) {
            try {
                if (!validator.isValid()) {
                    isValid = false;
                    addErrorMessage(validator.getErrorMessage());
                }
            } catch (ToolException te) {
                addErrorMessage(te.getMessage());
            }
        }

        // By default just use WsdlRefValidator
        if (!env.fullValidateWSDL()) {
            return isValid;
        }

        if (!StringUtils.isEmpty(schemaDir)) {
            schemaValidator = new SchemaValidator(schemaDir, wsdl, schemas);
        } else {
            try {
                schemaValidator = new SchemaValidator(getDefaultSchemas(), wsdl, schemas);
            } catch (IOException e) {
                throw new ToolException("Schemas can not be loaded before validating wsdl", e);
            }

        }
        if (!schemaValidator.isValid()) {
            this.addErrorMessage(schemaValidator.getErrorMessage());
            isValid = false;
        }
        return isValid;
    }

    public void validateAndCollect(AnalysisInformationCollector collector) {
        String validationException = null;
        try {
            isValid();
        } catch (ToolException te) {
            validationException = te.getMessage();
        }
        // collect errors
        for (AbstractDefinitionValidator validator : validators) {
            collect(validator, collector);
            /*if (validator instanceof WSDLRefValidator) {
                collect(validator, collector, "WSDLRefValidator");
            } else if (validator instanceof UniqueBodyPartsValidator) {
                collect(validator, collector, "UniqueBodyPartsValidator");
            } else if (validator instanceof WSIBasicProfileValidator) {
                collect(validator, collector, "WSDLRefValidator");
            } else if (validator instanceof MIMEBindingValidator) {
                collect(validator, collector, "WSDLRefValidator");
            }*/
        }

        if (validationException != null) {
            collector.addError(ASSERTION_ID, validationException, AnalysisInformationCollector.SEVERITY_LEVEL_CRITICAL);
        }
    }

    private void collect(AbstractDefinitionValidator validator, AnalysisInformationCollector collector) {
        String tag = validator.getClass().getSimpleName();
        ValidationResult result = validator.getValidationResults();
        Stack<String> errors = result.getErrors();
        Stack<String> warnings = result.getWarnings();
        System.out.println("Collecting " + tag);
        for (String error : errors) {
            System.out.println(tag + " error: " + error);
            collector.addError(ASSERTION_ID, error, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
        }
        for (String warning : warnings) {
            System.out.println(tag + " warning: " + warning);
            collector.addWarning(ASSERTION_ID, warning, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
        }
    }

    /*
    private void collect(WSDLRefValidator validator, AnalysisInformationCollector collector) {
        ValidationResult result = validator.getValidationResults();
        Stack<String> errors = result.getErrors();
        Stack<String> warnings = result.getWarnings();
        System.out.println("Collecting WSDLRefValidator");
        for (String error : errors) {
            System.out.println("WSDLRefValidator error: " + error);
            collector.addError(ASSERTION_ID, error, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
        }
        for (String warning : warnings) {
            System.out.println("WSDLRefValidator warning: " + warning);
            collector.addWarning(ASSERTION_ID, warning, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
        }
    }

    private void collect(UniqueBodyPartsValidator validator, AnalysisInformationCollector collector) {
        String error = validator.getErrorMessage();
        System.out.println("UniqueBodyPartsValidator: " + error);
        collector.addError(ASSERTION_ID, error, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
    }

    private void collect(WSIBasicProfileValidator validator, AnalysisInformationCollector collector) {
        String error = validator.getErrorMessage();
        System.out.println("WSIBasicProfileValidator: " + error);
        collector.addError(ASSERTION_ID, error, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
    }

    private void collect(MIMEBindingValidator validator, AnalysisInformationCollector collector) {
        String error = validator.getErrorMessage();
        System.out.println("MIMEBindingValidator: " + error);
        collector.addError(ASSERTION_ID, error, AnalysisInformationCollector.SEVERITY_LEVEL_UNKNOWN);
    }*/
}
