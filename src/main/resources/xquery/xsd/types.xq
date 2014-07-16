(:
returns information on each type defined in schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $types := $xmlSource//xsd:simpleType, $xmlSource//xsd:complexType

return
<result>
{
  for $type in $types
  return
  <item>
    <name>{string($type/@name)}</name>
    <node>{local-name($type)}</node>
  </item>
}
</result>
