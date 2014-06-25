(:
Extracts the <service>'s name in definitions/service

<result>
  <item>
    <name>service name</name>
  </item>
  <item>
    <name>service name</name>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $services := $xmlSource/wsdl:definitions/wsdl:service

return
<result>
{for $service in $services
  return
  <item>
    <name>{string($service/@name)}</name>
  </item>
}
</result>
