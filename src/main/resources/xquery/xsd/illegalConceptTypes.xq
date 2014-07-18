(:
returns information on types defined in schema that are illegal in concept schema
this script does not determine if the schema is a concept schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: find all top-level nodes with complex type content :)
let $allComplex := ($xmlSource/xsd:schema/xsd:complexType, $xmlSource/xsd:schema/*[//xsd:complexType])
(: allowed top-level simple types :)
let $allowed := $xmlSource/xsd:schema/xsd:simpleType[/xsd:restriction/xsd:enumeration]
(: find all top-level nodes with simple type that are not in the allowed list :)
let $allSimple := $xmlSource/xsd:schema/xsd:simpleType[not(.=$allowed)]

let $all := ($allComplex, $allSimple)
let $nodes := $xmlSource/xsd:schema/*[.=$all] (: to get distinct node list without defining functions :)

(:
let $all := $xmlSource/xsd:schema/*[/xsd:simpleType]
let $nodes := $all[not(.=$allowed)]
:)

let $imports := $xmlSource/xsd:schema/xsd:import
let $includes := $xmlSource/xsd:schema/xsd:include

return
<result>
{
  for $node in $nodes
  return
  <item>
    <name>{string($node/@name)}</name>
    <node>{local-name($node)}</node>
  </item>
}
{
  for $import in $imports
  return
  <item>
    <name>import</name>
    <node>{string($import/@namespace)}</node>
  </item>
}
{
  for $include in $includes
  return
  <item>
    <name>include</name>
    <node>{string($include/@schemaLocation)}</node>
  </item>
}
</result>
