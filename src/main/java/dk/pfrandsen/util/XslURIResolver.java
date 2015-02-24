package dk.pfrandsen.util;

import org.apache.commons.io.IOUtils;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// URI resolver that can only resolve one resource annotated-xsd.xsl (imported by annotated-wsdl.xsl)
// see /src/main/resources/xslt/tohtml
public class XslURIResolver implements URIResolver {
    static final String HREF = "annotated-xsd.xsl";
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String stylesheet = "/xslt/tohtml/annotated-xsd.xsl";
        try {
            if (HREF.equals(href)) {
                String xsl = IOUtils.toString(XslURIResolver.class.getResourceAsStream(stylesheet),
                        StandardCharsets.UTF_8);
                return new StreamSource(IOUtils.toInputStream(xsl));
            }
        } catch (IOException ignore) {
        }
        return null;
    }
}
