(:
Return list of operation names for binding
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare variable $xmlSource external;
declare variable $name external;

let $operations := $xmlSource/wsdl:definitions/wsdl:binding[@name=$name]/wsdl:operation

return
<result>
{for $operation in $operations
  return
  <item>
    <name>{string($operation/@name)}</name>
  </item>
}
</result>
