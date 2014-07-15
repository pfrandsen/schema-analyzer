(:
Extracts the target namespaces from the schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $tns := $xmlSource/xsd:schema/@targetNamespace

return
<result>
  <item>
    <namespaceUri>{string($tns)}</namespaceUri>
  </item>
</result>
