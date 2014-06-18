(:
Extracts the <simpleType>'s defined in definitions/types/schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $simpleTypes := $xmlSource/wsdl:definitions/wsdl:types/xsd:schema/xsd:simpleType

return
<result>
{for $simpleType in $simpleTypes
  return
  <item>
    <name>{string($simpleType/@name)}</name>
    <simpleType>{$simpleType}</simpleType>
  </item>
}
</result>
