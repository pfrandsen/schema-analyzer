
declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare variable $xmlSource external;
declare variable $name external;

let $soapBinding := $xmlSource/wsdl:definitions/wsdl:binding[@name=$name]/soap:binding

return if ($soapBinding)
then
<result>
  <item>
    <style>{string($soapBinding/@style)}</style>
    <transport>{string($soapBinding/@transport)}</transport>
  </item>
</result>
else
<result>
</result>

