package dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap;

import org.apache.cxf.binding.soap.wsdl.extensions.SoapHeader;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import java.util.ArrayList;
import java.util.List;

// These methods should be included in org.apache.cxf.binding.soap.SOAPBindingUtil

public final class SOAPBindingUtil {

    public static List<SoapHeader> getBindingInputSOAPHeaders(BindingOperation bop) {
        List<SoapHeader> headers = new ArrayList<SoapHeader>();
        BindingInput bindingInput = bop.getBindingInput();
        if (bindingInput != null) {
            for (Object obj : bindingInput.getExtensibilityElements()) {
                if (org.apache.cxf.binding.soap.SOAPBindingUtil.isSOAPHeader(obj)) {
                    headers.add(org.apache.cxf.binding.soap.SOAPBindingUtil.getProxy(SoapHeader.class, obj));
                }
            }
        }
        return headers;
    }

    public static List<SoapHeader> getBindingOutputSOAPHeaders(BindingOperation bop) {
        List<SoapHeader> headers = new ArrayList<SoapHeader>();
        BindingOutput bindingOutput = bop.getBindingOutput();
        if (bindingOutput != null) {
            for (Object obj : bindingOutput.getExtensibilityElements()) {
                if (org.apache.cxf.binding.soap.SOAPBindingUtil.isSOAPHeader(obj)) {
                    headers.add(org.apache.cxf.binding.soap.SOAPBindingUtil.getProxy(SoapHeader.class, obj));
                }
            }
        }
        return headers;
    }

}
