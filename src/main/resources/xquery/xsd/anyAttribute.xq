(:
return list of top level nodes that have an xsd:anyAttribute somewhere in its subtree
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: find all top-level nodes that include anyType attribute :)
let $withAnyAttribute := $xmlSource/xsd:schema/*[//xsd:anyAttribute]

return
<result>
{
  for $node in $withAnyAttribute
  return
  <item>
    <name>{string($node/@name)}</name>
    <node>{local-name($node)}</node>
  </item>
}
</result>

