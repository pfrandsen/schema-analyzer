(:
Extracts the <port>'s name for a given service name

<result>
  <item>
    <name>port name</name>
  </item>
  <item>
    <name>port name</name>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare variable $xmlSource external;
declare variable $name external;

let $servicePorts := $xmlSource/wsdl:definitions/wsdl:service[@name=$name]/wsdl:port

return
<result>
{for $port in $servicePorts
  return
  <item>
    <name>{string($port/@name)}</name>
  </item>
}
</result>
