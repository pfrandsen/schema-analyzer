(:
Extracts the <portType>'s name and documentation in definitions/portType

<result>
  <item>
    <name>port type name</name>
    <documentation>port type documentation</documentation>
  </item>
  <item>
    <name>port type name</name>
    <documentation>port type documentation</documentation>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $portTypes := $xmlSource/wsdl:definitions/wsdl:portType

return
<result>
{for $portType in $portTypes
  return
  <item>
    <name>{string($portType/@name)}</name>
    <documentation>{$portType/wsdl:documentation}</documentation>
  </item>
}
</result>
