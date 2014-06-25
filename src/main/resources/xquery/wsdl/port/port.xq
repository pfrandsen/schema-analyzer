(:
Extracts the <port>'s name and the enclosing service name in definitions/service/port

<result>
  <item>
    <name>port name</name>
    <service>service name</service>
  </item>
  <item>
    <name>port name</name>
    <service>service name</service>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $ports := $xmlSource/wsdl:definitions/wsdl:service/wsdl:port

return
<result>
{for $port in $ports
  let $service := $xmlSource/wsdl:definitions/wsdl:service[wsdl:port/@name = $port/@name]
  return
  <item>
    <name>{string($port/@name)}</name>
    <service>{string($service/@name)}</service>
  </item>
}
</result>


(:
The parent ref below fails - report error to EZH
return
<result>
{for $port in $ports
  return
  <item>
    <name>{string($port/@name)}</name>
    <service>{string($port/../@name)}</service>
  </item>
}
</result>
:)