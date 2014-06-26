package dk.pfrandsen.util;

import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.QName;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.xdmio.XDMAtomicItemFactory;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XMLSource;
import dk.pfrandsen.Xml;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XQuery {

    public static void addNamespace(StringBuilder builder, String prefix, String namespace) {
        builder.append("declare namespace ").append(prefix).append("=\"").append(namespace).append("\";\n");
    }

    public static String getXQuery(String queryName) {
        InputStream stream = XQuery.class.getResourceAsStream("/wsdl/xquery/" + queryName);
        try {
            return IOUtils.toString(stream);
        } catch (IOException e) {
            return "";
        }
    }

    public static String runXQuery(Path queryLocation, String queryFile, String xml) throws IOException,
            MXQueryException {
        String queryResult = "";
        String path = "/xquery/" + queryLocation.resolve(queryFile).toString();
        // InputStream stream = XQuery.class.getResourceAsStream(path);
        String xq = IOUtils.toString(XQuery.class.getResourceAsStream(path));

        Context ctx = new Context();
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setSchemaAwareness(true);
        XQCompiler compiler = new CompilerImpl();
        PreparedStatement statement;
        statement = compiler.compile(ctx, xq, compilerOptions);
        XDMIterator result;
        result = statement.evaluate();
        XMLSource xmlIt = XDMInputFactory.createXMLInput(result.getContext(), new StringReader(xml), true,
                Context.NO_VALIDATION, QueryLocation.OUTSIDE_QUERY_LOC);
        statement.addExternalResource(new QName("xmlSource"), xmlIt);
        // Create an XDM serializer, can take an XDMSerializerSettings object if needed
        XDMSerializer ip = new XDMSerializer();
        // run expression, generate XDM instance and serialize into String format
        queryResult = ip.eventsToXML(result);
        // XQuery Update "programs" create a pending update list, not a normal result
        // apply the results to the relevant "stores"
        // currently in-memory stores
        statement.applyPUL();
        statement.close();

        // System.out.println(strResult);
        return queryResult;
    }

    public static String runXQuery(Path queryLocation, String queryFile, String xml, String name) throws IOException,
            MXQueryException {
        String queryResult = "";
        String path = "/xquery/" + queryLocation.resolve(queryFile).toString();
        // InputStream stream = XQuery.class.getResourceAsStream(path);
        String xq = IOUtils.toString(XQuery.class.getResourceAsStream(path));

        Context ctx = new Context();
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setSchemaAwareness(true);
        XQCompiler compiler = new CompilerImpl();
        PreparedStatement statement;
        statement = compiler.compile(ctx, xq, compilerOptions);
        XDMIterator result;
        result = statement.evaluate();

        // add external resources
        XMLSource xmlIt = XDMInputFactory.createXMLInput(result.getContext(), new StringReader(xml), true,
                Context.NO_VALIDATION, QueryLocation.OUTSIDE_QUERY_LOC);
        statement.addExternalResource(new QName("xmlSource"), xmlIt);
        statement.addExternalResource(new QName("name"), XDMAtomicItemFactory.createString(name));

        // Create an XDM serializer, can take an XDMSerializerSettings object if needed
        XDMSerializer ip = new XDMSerializer();
        // run expression, generate XDM instance and serialize into String format
        queryResult = ip.eventsToXML(result);
        // XQuery Update "programs" create a pending update list, not a normal result
        // apply the results to the relevant "stores"
        // currently in-memory stores
        statement.applyPUL();
        statement.close();

        // System.out.println(strResult);
        return queryResult;
    }

    /**
     *
     * @param xqResult
     * @param key
     * @return list of values for the given key
     */
    public static List<String> mapResult(String xqResult, String key) {
        List<Map<String, String>> result = Xml.parseXQueryResult(xqResult);
        List<String> retVal = new ArrayList<>();
        if (result.size() > 0) {
            for (Map<String, String> element : result) {
                retVal.add(element.get(key));
            }
        }
        return retVal;
    }

}
