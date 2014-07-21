(:
return list of imported namespaces
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $namespaces := distinct-values($xmlSource//xsd:import/string(@namespace))

return
<result>
{
  for $namespace in $namespaces
  return
  <item>
    <namespace>{$namespace}</namespace>
  </item>
}
</result>
