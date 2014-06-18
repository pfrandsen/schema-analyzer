package dk.pfrandsen.wsdl.cxf.tools.validator;

import dk.pfrandsen.check.AnalysisInformation;
import dk.pfrandsen.check.AnalysisInformationCollector;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.util.URIParserUtil;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import upstream.org.apache.cxf.tools.validator.internal.WSDL11Validator;

import java.net.URI;

/**
 * Based on org.apache.cxf.tools.validator.WSDLValidator and related classes
 */
public class Validator {
    public static String ASSERTION_ID = "CXF-WSDL-Validation";

    public static Bus getBus(String catalogLocation) {
        Bus bus = BusFactory.getDefaultBus();

        OASISCatalogManager catalogManager = bus.getExtension(OASISCatalogManager.class);

        //String catalogLocation = getCatalogURL();
        if (!StringUtils.isEmpty(catalogLocation)) {
            try {
                catalogManager.loadCatalog(new URI(catalogLocation).toURL());
            } catch (Exception e) {
                e.printStackTrace();
                // throw new ToolException(new Message("FOUND_NO_FRONTEND", LOG, catalogLocation));
            }
        }

        return bus;
    }

    public static void checkWsdl(String wsdlUrl, AnalysisInformationCollector collector) {
        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
        context.put(ToolConstants.CFG_VALIDATE_WSDL, "all");
        // context.put(ToolConstants.CFG_CMD_ARG, getArgument());
        System.out.println("CFG_SUPPRESS_WARNINGS: " + context.optionSet(ToolConstants.CFG_SUPPRESS_WARNINGS));

        System.out.println("CFG_SCHEMA_URL" + ToolConstants.CFG_SCHEMA_URL);
        System.out.println("CFG_SCHEMA_DIR" + ToolConstants.CFG_SCHEMA_DIR);
        // CFG_SCHEMA_URL is a String[] with a list of urls to locate schemas for the schema validator part of the
        // WSDL11Validator
        // CFG_SCHEMA_DIR is a directory where the validator looks for schema files; it can be set in the context or as
        // a system property where the context has priority; if not set schemas will be loaded from jar file

        context.put(ToolConstants.CFG_WSDLURL, wsdlUrl);
        Wsdl11Validator wsdlValidator = new Wsdl11Validator(null, context, getBus(getCatalogURL(context)));

        wsdlValidator.validateAndCollect(collector);
    }

    protected static String getCatalogURL(ToolContext context) {
        String catalogLocation = (String) context.get(ToolConstants.CFG_CATALOG);
        return URIParserUtil.getAbsoluteURI(catalogLocation);
    }

}


