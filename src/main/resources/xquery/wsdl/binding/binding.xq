(:
Extracts the <binding>'s name, type, and documentation in definitions/binding

<result>
  <item>
    <name>binding name</name>
    <type>binding type</type>
    <documentation>Binding documentation</documentation>
  </item>
  <item>
    <name>binding name</name>
    <type>binding type</type>
    <documentation>Binding documentation</documentation>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $bindings := $xmlSource/wsdl:definitions/wsdl:binding

return
<result>
{for $binding in $bindings
  return
  <item>
    <name>{string($binding/@name)}</name>
    <type>{string($binding/@type)}</type>
    <documentation>{$binding/wsdl:documentation}</documentation>
  </item>
}
</result>
