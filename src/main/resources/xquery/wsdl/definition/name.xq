(:
Extracts the name attribute in the wsdl definitions element
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $name := $xmlSource/wsdl:definitions/@name

return
<result>
  <item>
    <name>{string($name)}</name>
  </item>
</result>
