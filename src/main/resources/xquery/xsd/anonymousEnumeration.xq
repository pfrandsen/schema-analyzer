(:
returns information on embedded (anonymous) enumerations defined in schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: anonymous enumerations are not allowed :)
let $all := $xmlSource/xsd:schema/*[//xsd:enumeration] (: top-level node with embedded enumeration:)
let $allowed := $xmlSource/xsd:schema/xsd:simpleType[/xsd:restriction/xsd:enumeration]
let $nodes := $all[not(.=$allowed)]

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
</result>
