(:
Extracts the <include's defined in definitions/types/schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $includes := $xmlSource/wsdl:definitions/wsdl:types/xsd:schema/xsd:include

return
<result>
{for $include in $includes
  return
  <item>
    <schemaLocation>{string($include/@schemaLocation)}</schemaLocation>
  </item>
}
</result>
