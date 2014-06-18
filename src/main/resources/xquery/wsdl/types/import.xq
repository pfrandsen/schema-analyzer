(:
Extracts the <import>'s defined in definitions/types/schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $imports := $xmlSource/wsdl:definitions/wsdl:types/xsd:schema/xsd:import

return
<result>
{for $import in $imports
  return
  <item>
    <namespace>{string($import/@namespace)}</namespace>
    <schemaLocation>{string($import/@schemaLocation)}</schemaLocation>
  </item>
}
</result>
