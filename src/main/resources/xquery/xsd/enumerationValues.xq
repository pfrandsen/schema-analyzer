(:
return all enumeration values including information about the top-level node they are defined in
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $all := $xmlSource/xsd:schema/*[//xsd:enumeration] (: top-level nodes with embedded enumeration:)

return
<result>
{
  for $node in $all
    for $value in $node//xsd:enumeration
  return
  <item>
    <name>{string($node/@name)}</name>
    <node>{local-name($node)}</node>
    <value>{string($value/@value)}</value>
  </item>
}
</result>

