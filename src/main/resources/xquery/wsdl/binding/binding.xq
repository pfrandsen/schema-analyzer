(:
Extracts the <binding>'s name, type, documentation, and resolved type (portType) information in definitions/binding

<result>
  <item>
    <name>binding name</name>
    <type>binding type</type>
    <documentation>Binding documentation</documentation>
    <type-local>type local name</type-local>
    <type-ns>type namespace</type-ns>
    <type-pre>type prefix</type-pre>
  </item>
  <item>
    <name>binding name</name>
    <type>binding type</type>
    <documentation>Binding documentation</documentation>
    <type-local>type local name</type-local>
    <type-ns>type namespace</type-ns>
    <type-pre>type prefix</type-pre>
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
  let $type_qn := resolve-QName($binding/@type, $binding)
  let $type_local := if (exists($type_qn)) then local-name-from-QName($type_qn) else ''
  let $type_ns := if (exists($type_qn)) then namespace-uri-from-QName($type_qn) else ''
  let $type_pre := if (exists($type_qn)) then prefix-from-QName($type_qn) else ''
  return
  <item>
    <name>{string($binding/@name)}</name>
    <type>{string($binding/@type)}</type>
    <documentation>{$binding/wsdl:documentation}</documentation>
    <type-local>{$type_local}</type-local>
    <type-ns>{$type_ns}</type-ns>
    <type-pre>{$type_pre}</type-pre>
  </item>
}
</result>
