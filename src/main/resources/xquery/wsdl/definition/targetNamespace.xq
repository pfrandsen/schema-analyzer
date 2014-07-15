(:
Extracts the target namespaces in the wsdl definitions element
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $tns := $xmlSource/wsdl:definitions/@targetNamespace

return
<result>
  <item>
    <namespaceUri>{string($tns)}</namespaceUri>
  </item>
</result>
