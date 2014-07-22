(:
return list of top level nodes that have an attribute with xsd:anyType value somewhere in its subtree
it is assumed that the xml schema namespace uses prefix xs or xsd
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: find all top-level nodes that include anyType attribute :)
let $withAnyType := for $x in $xmlSource/xsd:schema/* return
   if ($x//@*[contains(., "xs:anyType") or contains(., "xsd:anyType")]) then $x else ()

return
<result>
{
  for $anyType in $withAnyType
  return
  <item>
    <name>{string($anyType/@name)}</name>
    <node>{local-name($anyType)}</node>
  </item>
}
</result>

