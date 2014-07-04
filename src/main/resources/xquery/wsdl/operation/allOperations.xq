(:
Extracts the <operation>'s for all portTypes in definitions/portType
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $operations := $xmlSource/wsdl:definitions/wsdl:portType/wsdl:operation

return
<result>
{for $operation in $operations
  return
  <item>
    <name>{string($operation/@name)}</name>
  </item>
}
</result>
