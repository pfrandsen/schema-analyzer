
declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare namespace soap="http://schemas.xmlsoap.org/wsdl/soap/";
declare namespace soap12="http://schemas.xmlsoap.org/wsdl/soap12/";
declare variable $xmlSource external;
declare variable $name external;

let $soapBinding := $xmlSource/wsdl:definitions/wsdl:binding[@name=$name]/soap:binding
let $soap12Binding := $xmlSource/wsdl:definitions/wsdl:binding[@name=$name]/soap12:binding

return if ($soapBinding)
then
<result>
  <item>
    <style>{string($soapBinding/@style)}</style>
    <transport>{string($soapBinding/@transport)}</transport>
    <version>1.1</version>
  </item>
</result>
else if ($soap12Binding)
  then
    <result>
      <item>
        <style>{string($soap12Binding/@style)}</style>
        <transport>{string($soap12Binding/@transport)}</transport>
        <version>1.2</version>
      </item>
    </result>
else
<result>
</result>

