package upstream.org.apache.cxf.tools.validator.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SOAPBindingUtil;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapBody;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapFault;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapHeader;
import org.apache.cxf.common.util.CollectionUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.wsdl.WSDLHelper;

// WS-I Basic Profile 1.1 specification: http://www.ws-i.org/Profiles/BasicProfile-1.1-2006-04-10.html
public class WSIBPValidator extends AbstractDefinitionValidator {
    private List<String> operationMap = new ArrayList<String>();
    private WSDLHelper wsdlHelper = new WSDLHelper();

    public WSIBPValidator(Definition def) {
        super(def);
    }

    public boolean isValid() {
        boolean valid = true;
        for (Method m : getClass().getMethods()) {
            if (m.getName().startsWith("check") && m.getGenericReturnType() == boolean.class
                    && m.getGenericParameterTypes().length == 0) {
                try {
                    Boolean res = (Boolean) m.invoke(this);
                    if (!res) {
                        valid = false;
                    }
                } catch (Exception e) {
                    throw new ToolException(e);
                }
            }
        }
        return valid;
    }

    // R2716 A document-literal binding in a DESCRIPTION MUST NOT have the namespace attribute specified on contained
    // soap:body, soap:header, soap:headerfault and soap:fault elements.
    private boolean checkR2716(final BindingOperation bop) {
        // headerfault which can be embedded in both input and output headers have the same namespace attribute rule
        // are not validated. http://www.w3.org/TR/wsdl#_soap:header
        // TODO: should also check headerfault
        boolean retVal = true;
        SoapBody inSoapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        if (inSoapBody != null && !StringUtils.isEmpty(inSoapBody.getNamespaceURI())) {
            addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2716") + "Operation '"
                    + bop.getName() + "' input soapBody MUST NOT have namespace attribute");
            retVal = false;
        }
        SoapBody outSoapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (outSoapBody != null && !StringUtils.isEmpty(outSoapBody.getNamespaceURI())) {
            addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2716") + "Operation '"
                    + bop.getName() + "' output soapBody MUST NOT have namespace attribute");
            retVal = false;
        }

        List<SoapHeader> inputHeaders =
                dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingInputSOAPHeaders(bop);
        for (SoapHeader hdr : inputHeaders) {
            if (!StringUtils.isEmpty(hdr.getNamespaceURI())) {
                String msg = hdr.getMessage() == null ? "" : "<" + hdr.getMessage().getLocalPart() + "> ";
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2716") + "Operation '"
                        + bop.getName() + "' input soapHeader " + msg + "MUST NOT have namespace attribute");
                retVal = false;
            }
        }
        List<SoapHeader> outputHeaders =
            dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingOutputSOAPHeaders(bop);
        for (SoapHeader hdr : outputHeaders) {
            if (!StringUtils.isEmpty(hdr.getNamespaceURI())) {
                String msg = hdr.getMessage() == null ? "" : "<" + hdr.getMessage().getLocalPart() + "> ";
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2716") + "Operation '"
                        + bop.getName() + "' output soapHeader " + msg + "MUST NOT have namespace attribute");
                retVal = false;
            }
        }

        List<SoapFault> soapFaults = SOAPBindingUtil.getBindingOperationSoapFaults(bop);
        for (SoapFault fault : soapFaults) {
            if (!StringUtils.isEmpty(fault.getNamespaceURI())) {
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2716") + "Operation '"
                        + bop.getName() + "' soapFault <" + fault.getName() + "> MUST NOT have namespace attribute");
                retVal = false;
            }
        }
        return retVal;
    }

    // R2717 An rpc-literal binding in a DESCRIPTION MUST have the namespace attribute specified, the value of which
    // MUST be an absolute URI, on contained soap:body elements.
    // R2726 An rpc-literal binding in a DESCRIPTION MUST NOT have the namespace attribute specified on contained
    // soap:header, soap:headerfault and soap:fault elements.
    private boolean checkR2717AndR2726(final BindingOperation bop) {
        // TODO: should also check headerfault
        boolean retVal = true;
        if (null == bop) {
            return true;
        }
        SoapBody inSoapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        if (inSoapBody != null && StringUtils.isEmpty(inSoapBody.getNamespaceURI())) {
            addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2717")
                    + "soapBody in the input of the binding operation '"
                    + bop.getName() + "' MUST have namespace attribute");
            retVal = false;
        }
        SoapBody outSoapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (outSoapBody != null && StringUtils.isEmpty(outSoapBody.getNamespaceURI())) {
            addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2717")
                    + "soapBody in the output of the binding operation '"
                    + bop.getName() + "' MUST have namespace attribute");
            retVal = false;
        }

        List<SoapHeader> inputHeaders =
                dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingInputSOAPHeaders(bop);
        for (SoapHeader hdr : inputHeaders) {
            if (!StringUtils.isEmpty(hdr.getNamespaceURI())) {
                String msg = "";
                if (hdr.getMessage() != null) {
                    msg = "<" + hdr.getMessage().getLocalPart() + "," + hdr.getPart() +"> ";
                }
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2726") + "Operation '"
                        + bop.getName() + "' input soapHeader " + msg + "MUST NOT have namespace attribute");
                retVal = false;
            }
        }
        List<SoapHeader> outputHeaders =
                dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingOutputSOAPHeaders(bop);
        for (SoapHeader hdr : outputHeaders) {
            if (!StringUtils.isEmpty(hdr.getNamespaceURI())) {
                String msg = "";
                if (hdr.getMessage() != null) {
                    msg = "<" + hdr.getMessage().getLocalPart() + "," + hdr.getPart() +"> ";
                }
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2726") + "Operation '"
                        + bop.getName() + "' output soapHeader " + msg + "MUST NOT have namespace attribute");
                retVal = false;
            }
        }

        List<SoapFault> soapFaults = SOAPBindingUtil.getBindingOperationSoapFaults(bop);
        for (SoapFault fault : soapFaults) {
            if (!StringUtils.isEmpty(fault.getNamespaceURI())) {
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2726") + "Operation '"
                        + bop.getName() + "' soapFault <" + fault.getName() + "> MUST NOT have namespace attribute");
                retVal = false;
            }
        }
        return retVal;
    }

    private boolean checkR2201Input(final Operation operation,
                                    final BindingOperation bop) {
        List<Part> partsList = wsdlHelper.getInMessageParts(operation);
        int inmessagePartsCount = partsList.size();
        SoapBody soapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        if (soapBody != null) {
            List<?> parts = soapBody.getParts();
            int boundPartSize = parts == null ? inmessagePartsCount : parts.size();
            SoapHeader soapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
            boundPartSize = soapHeader != null
                    && soapHeader.getMessage().equals(
                    operation.getInput().getMessage()
                            .getQName())
                    ? boundPartSize - 1 : boundPartSize;

            if (parts != null) {
                Iterator<?> partsIte = parts.iterator();
                while (partsIte.hasNext()) {
                    String partName = (String)partsIte.next();
                    boolean isDefined = false;
                    for (Part part : partsList) {
                        if (partName.equalsIgnoreCase(part.getName())) {
                            isDefined = true;
                            break;
                        }
                    }
                    if (!isDefined) {
                        addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2201") + "Operation '"
                                + operation.getName() + "' soapBody parts : "
                                + partName + " not found in the message, wrong WSDL");
                        return false;
                    }
                }
            } else {
                if (partsList.size() > 1) {
                    addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2210") + "Operation '" + operation.getName()
                            + "' more than one part bound to body");
                    return false;
                }
            }


            if (boundPartSize > 1) {
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2201") + "Operation '" + operation.getName()
                        + "' more than one part bound to body");
                return false;
            }
        }
        return true;
    }

    private boolean checkR2201Output(final Operation operation,
                                     final BindingOperation bop) {
        int outmessagePartsCount = wsdlHelper.getOutMessageParts(operation).size();
        SoapBody soapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (soapBody != null) {
            List<?> parts = soapBody.getParts();
            int boundPartSize = parts == null ? outmessagePartsCount : parts.size();
            SoapHeader soapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
            boundPartSize = soapHeader != null
                    && soapHeader.getMessage().equals(
                    operation.getOutput().getMessage()
                            .getQName())
                    ? boundPartSize - 1 : boundPartSize;
            if (parts != null) {
                Iterator<?> partsIte = parts.iterator();
                while (partsIte.hasNext()) {
                    String partName = (String)partsIte.next();
                    boolean isDefined = false;
                    for (Part part : wsdlHelper.getOutMessageParts(operation)) {
                        if (partName.equalsIgnoreCase(part.getName())) {
                            isDefined = true;
                            break;
                        }
                    }
                    if (!isDefined) {
                        addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2201") + "Operation '"
                                + operation.getName() + "' soapBody parts : "
                                + partName + " not found in the message, wrong WSDL");
                        return false;
                    }

                }
            } else {
                if (wsdlHelper.getOutMessageParts(operation).size() > 1) {
                    addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2210") + "Operation '" + operation.getName()
                            + "' more than one part bound to body");
                    return false;
                }
            }

            if (boundPartSize > 1) {
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2201") + "Operation '" + operation.getName()
                        + "' more than one part bound to body");
                return false;
            }
        }
        return true;
    }

    public boolean checkBinding() {
        for (PortType portType : wsdlHelper.getPortTypes(def)) {
            Iterator<?> ite = portType.getOperations().iterator();
            while (ite.hasNext()) {
                Operation operation = (Operation)ite.next();
                if (isOverloading(operation.getName())) {
                    continue;
                }
                BindingOperation bop = wsdlHelper.getBindingOperation(def, operation.getName());
                if (bop == null) {
                    addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2718")
                            + "A wsdl:binding in a DESCRIPTION MUST have the same set of "
                            + "wsdl:operations as the wsdl:portType to which it refers. "
                            + operation.getName() + " not found in wsdl:binding.");
                    return false;
                }
                Binding binding = wsdlHelper.getBinding(bop, def);
                String bindingStyle = binding != null ? SOAPBindingUtil.getBindingStyle(binding) : "";
                String style = StringUtils.isEmpty(SOAPBindingUtil.getSOAPOperationStyle(bop))
                        ? bindingStyle : SOAPBindingUtil.getSOAPOperationStyle(bop);
                if ("DOCUMENT".equalsIgnoreCase(style) || StringUtils.isEmpty(style)) {
                    boolean passed = checkR2201Input(operation, bop)
                            && checkR2201Output(operation, bop)
                            && checkR2716(bop);
                    if (!passed) {
                        return false;
                    }
                } else {
                    if (!checkR2717AndR2726(bop)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isHeaderPart(final List<SoapHeader> headers, final Part part) {
        String partName = part.getName();
        //QName elementName = part.getElementName();
        //if (elementName != null) {
        if (partName != null) {
            //String partName = elementName.getLocalPart();
            for (SoapHeader hdr : headers) {
                System.out.println("Checking " + partName + "," + hdr.getPart());
                if (partName.equals(hdr.getPart())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInputHeaderPart(final BindingOperation bop, final Part part) {
        List<SoapHeader> headers =
                dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingInputSOAPHeaders(bop);
        return isHeaderPart(headers, part);
        /* QName elementName = part.getElementName();
        if (elementName != null) {
            String partName = elementName.getLocalPart();
            List<SoapHeader> headers =
                    dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingInputSOAPHeaders(bop);
            for (SoapHeader hdr : headers) {
                if (partName.equals(hdr.getPart())) {
                    return true;
                }
            }
        }
        return false; */
    }

    private boolean isOutputHeaderPart(final BindingOperation bop, final Part part) {
        List<SoapHeader> headers =
                dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingOutputSOAPHeaders(bop);
        return isHeaderPart(headers, part);

        /* QName elementName = part.getElementName();
        if (elementName != null) {
            String partName = elementName.getLocalPart();

            // must check all headers!!
            List<SoapHeader> inputHeaders =
                    dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingInputSOAPHeaders(bop);

            List<SoapHeader> outputHeaders =
                    dk.pfrandsen.wsdl.upstream.org.apache.cxf.binding.soap.SOAPBindingUtil.getBindingOutputSOAPHeaders(bop);
            for (SoapHeader hdr : outputHeaders) {

            }

            SoapHeader inSoapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
            if (inSoapHeader != null) {
                return partName.equals(inSoapHeader.getPart());
            }
            SoapHeader outSoapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
            if (outSoapHeader != null) {
                return partName.equals(outSoapHeader.getPart());
            }
        }
        return false;*/
    }

    /*
    private boolean isHeaderPart(final BindingOperation bop, final Part part) {
        QName elementName = part.getElementName();
        if (elementName != null) {
            String partName = elementName.getLocalPart();
            SoapHeader inSoapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
            if (inSoapHeader != null) {
                return partName.equals(inSoapHeader.getPart());
            }
            SoapHeader outSoapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
            if (outSoapHeader != null) {
                return partName.equals(outSoapHeader.getPart());
            }
        }
        return false;
    } */

    public boolean checkR2203And2204() {

        Collection<Binding> bindings = CastUtils.cast(def.getBindings().values());
        for (Binding binding : bindings) {

            String style = SOAPBindingUtil.getCanonicalBindingStyle(binding);

            if (binding.getPortType() == null) {
                return true;
            }

            //

            for (Iterator<?> ite2 = binding.getPortType().getOperations().iterator(); ite2.hasNext();) {
                Operation operation = (Operation)ite2.next();
                BindingOperation bop = wsdlHelper.getBindingOperation(def, operation.getName());
                if (operation.getInput() != null && operation.getInput().getMessage() != null) {
                    Message inMess = operation.getInput().getMessage();

                    for (Iterator<?> ite3 = inMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (SOAPBinding.Style.RPC.name().equalsIgnoreCase(style) && p.getTypeName() == null
                                && !isInputHeaderPart(bop, p)) {

                            addErrorMessage("An rpc-literal binding in a DESCRIPTION MUST refer, "
                                    + "in its soapbind:body element(s), only to "
                                    + "wsdl:part element(s) that have been defined "
                                    + "using the type attribute.");
                            addErrorMessage("An rpc-literal binding " + p.getName());
                            return false;
                        }

                        if (SOAPBinding.Style.DOCUMENT.name().equalsIgnoreCase(style)
                                && p.getElementName() == null) {
                            addErrorMessage("A document-literal binding in a DESCRIPTION MUST refer, "
                                    + "in each of its soapbind:body element(s),"
                                    + "only to wsdl:part element(s)"
                                    + " that have been defined using the element attribute.");
                            return false;
                        }

                    }
                }
                if (operation.getOutput() != null && operation.getOutput().getMessage() != null) {
                    Message outMess = operation.getOutput().getMessage();
                    for (Iterator<?> ite3 = outMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (style.equalsIgnoreCase(SOAPBinding.Style.RPC.name()) && p.getTypeName() == null
                                &&  !isOutputHeaderPart(bop, p)) {
                            addErrorMessage("An rpc-literal binding in a DESCRIPTION MUST refer, "
                                    + "in its soapbind:body element(s), only to "
                                    + "wsdl:part element(s) that have been defined "
                                    + "using the type attribute.");
                            return false;
                        }

                        if (style.equalsIgnoreCase(SOAPBinding.Style.DOCUMENT.name())
                                && p.getElementName() == null) {
                            addErrorMessage("A document-literal binding in a DESCRIPTION MUST refer, "
                                    + "in each of its soapbind:body element(s),"
                                    + "only to wsdl:part element(s)"
                                    + " that have been defined using the element attribute.");
                            return false;
                        }

                    }
                }
            }

        }
        return true;
    }

    // TODO: Should also check SoapHeader/SoapHeaderFault
    public boolean checkR2205() {
        Collection<Binding> bindings = CastUtils.cast(def.getBindings().values());
        for (Binding binding : bindings) {

            if (!SOAPBindingUtil.isSOAPBinding(binding)) {
                addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2205") + "WSIBP Validator found <" + binding.getQName()
                        + "> is NOT a SOAP binding");
                continue;
            }
            if (binding.getPortType() == null) {
                //will error later
                continue;
            }

            // TODO: PFRANDSEN check headers - check that all headers have element attribute
            // get all input headers and output header for binding and check for element attribute (error if not present)
            // plus type attribute must not be present (warn)

            for (Iterator<?> ite2 = binding.getPortType().getOperations().iterator(); ite2.hasNext();) {
                Operation operation = (Operation)ite2.next();
                Collection<Fault> faults = CastUtils.cast(operation.getFaults().values());
                if (CollectionUtils.isEmpty(faults)) {
                    continue;
                }

                for (Fault fault : faults) {
                    Message message = fault.getMessage();
                    Collection<Part> parts = CastUtils.cast(message.getParts().values());
                    for (Part part : parts) {
                        if (part.getElementName() == null) {
                            addErrorMessage(getErrorPrefix("WSI-BP-1.0 R2205") + "In Message "
                                    + message.getQName() + ", part " + part.getName()
                                    + " must specify a 'element' attribute");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkR2705() {
        Collection<Binding> bindings = CastUtils.cast(def.getBindings().values());
        for (Binding binding : bindings) {
            if (SOAPBindingUtil.isMixedStyle(binding)) {
                addErrorMessage("Mixed style, invalid WSDL");
                return false;
            }
        }
        return true;
    }

    private boolean isOverloading(String operationName) {
        if (operationMap.contains(operationName)) {
            return true;
        } else {
            operationMap.add(operationName);
        }
        return false;
    }

    private static String getErrorPrefix(String ruleBroken) {
        return ruleBroken + " violation: ";
    }
}
