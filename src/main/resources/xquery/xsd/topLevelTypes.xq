(:
returns information on each top-level type defined in schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $types := ($xmlSource/xsd:schema/xsd:simpleType, $xmlSource/xsd:schema/xsd:complexType)

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
