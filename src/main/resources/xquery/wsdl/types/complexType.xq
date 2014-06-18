(:
Extracts the <complexType>'s defined in definitions/types/schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $complexTypes := $xmlSource/wsdl:definitions/wsdl:types/xsd:schema/xsd:complexType

return
<result>
{for $complexType in $complexTypes
  return
  <item>
    <name>{string($complexType/@name)}</name>
    <complexType>{$complexType}</complexType>
  </item>
}
</result>
