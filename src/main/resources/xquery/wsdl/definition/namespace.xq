(:
Extracts the namespaces in the wsdl definitions element
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $prefixes := in-scope-prefixes($xmlSource/wsdl:definitions)

return
<result>
{for $prefix in $prefixes
  return
  <item>
    <prefix>{$prefix}</prefix>
    <namespaceUri>{namespace-uri-for-prefix($prefix, $xmlSource/wsdl:definitions)}</namespaceUri>
  </item>
}
  <item>
    <prefix>targetNamespace</prefix>
    <namespaceUri>{string($xmlSource/wsdl:definitions/@targetNamespace)}</namespaceUri>
  </item>
</result>

