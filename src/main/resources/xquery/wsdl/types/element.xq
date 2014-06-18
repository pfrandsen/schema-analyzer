(:
Extracts the <element>'s defined in definitions/types/schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $elements := $xmlSource/wsdl:definitions/wsdl:types/xsd:schema/xsd:element

return
<result>

{for $element in $elements
  return
  <item>
    <name>{string($element/@name)}</name>
    <element>{$element}</element>
  </item>
}

</result>
