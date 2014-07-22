(:
return list of top level nodes that have an xsd:any somewhere in its subtree
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: find all top-level nodes that include any :)
let $withAny := $xmlSource/xsd:schema/*[//xsd:any]

return
<result>
{
  for $node in $withAny
  return
  <item>
    <name>{string($node/@name)}</name>
    <node>{local-name($node)}</node>
  </item>
}
</result>

