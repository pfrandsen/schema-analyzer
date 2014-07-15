(:
returns each element in schema that has nillable attribute set
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $elements := $xmlSource//xsd:element[exists(@nillable)]

return
<result>
{
  for $element in $elements
  return
  <item>
    <name>{string($element/@name)}</name>
    <nillable>{string($element/@nillable)}</nillable>
  </item>
}
</result>
