(:
returns each element in schema that has minOccurs="1" or maxOccurs="1"
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $nodes := $xmlSource//node()[@minOccurs='1' or @maxOccurs='1']

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
