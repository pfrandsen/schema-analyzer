
(:
Get soap:address location for given service and port
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare variable $xmlSource external;
declare variable $name external;  (: service name :)
declare variable $name2 external; (: port name :)

let $location := $xmlSource/wsdl:definitions/wsdl:service[@name=$name]/wsdl:port[@name=$name2]/soap:address/@location

return
<result>
  <item>
    <location>{string($location)}</location>
  </item>
</result>
