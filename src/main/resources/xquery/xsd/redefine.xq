(:
return list of redefinitions
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $redefines := $xmlSource//xsd:redefine

return
<result>
{
  for $redefine in $redefines
  return
  <item>
    <location>{string($redefine/@schemaLocation)}</location>
  </item>
}
</result>
