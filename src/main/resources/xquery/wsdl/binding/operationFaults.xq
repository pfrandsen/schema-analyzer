(:
Get the information about faults a given binding and operation.

<result>
  <item>
    <name>fault name</name>
    <soap-fault-name>soap fault name (child node)</soap-fault-name>
    <soap-fault-use>soap fault use (typically literal)</soap-fault-use>
  </item>
  <item>
    <name>fault name</name>
    <soap-fault-name>soap fault name (child node)</soap-fault-name>
    <soap-fault-use>soap fault use (typically literal)</soap-fault-use>
  </item>
  ...
</result>

:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare variable $xmlSource external;
declare variable $name external;
declare variable $name2 external;

let $faults := $xmlSource/wsdl:definitions/wsdl:binding[@name=$name]/wsdl:operation[@name=$name2]/wsdl:fault

return
<result>
{for $fault in $faults
  let $soapFaultName := if (exists($fault/soap:fault/@name))
    then string($fault/soap:fault/@name) else ''
  let $soapFaultUse := if (exists($fault/soap:fault/@use))
    then string($fault/soap:fault/@use) else ''
  return
  <item>
    <name>{string($fault/@name)}</name>
    <soap-fault-name>{$soapFaultName}</soap-fault-name>
    <soap-fault-use>{$soapFaultUse}</soap-fault-use>
  </item>
}
</result>

