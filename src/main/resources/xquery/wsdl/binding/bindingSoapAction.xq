(:
Get the soapAction values for a given binding. If an operation does not have a soapAction an empty string will be used.

Returns list of items with operation name and corresponding soap action uri
<result>
  <item>
    <name>operation name</name>
    <soapAction>soap action uri</soapAction>
  </item>
  <item>
    <name>operation name</name>
    <soapAction>soap action uri</soapAction>
  </item>
  ...
</result>

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
  let $soapAction := if (exists($operation/soap:operation/@soapAction))
    then string($operation/soap:operation/@soapAction) else ''
  return
  <item>
    <name>{string($operation/@name)}</name>
    <soapAction>{$soapAction}</soapAction>
  </item>
}
</result>

