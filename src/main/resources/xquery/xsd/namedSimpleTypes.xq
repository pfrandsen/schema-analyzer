(:
return list of all names simple types
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $all := $xmlSource//xsd:simpleType[exists(@name)] (: all named simple types :)

return
<result>
{
  for $node in $all
  return
  <item>
    <name>{string($node/@name)}</name>
  </item>
}
</result>

