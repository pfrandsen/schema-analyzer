(: return list of names for all explicitly named elements :)
declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $namedElements :=  $xmlSource//xsd:element[exists(@name)]

return
<result>
{
  for $namedElement in $namedElements
  return
  <item>
    <name>{string($namedElement/@name)}</name>
  </item>
}
</result>
