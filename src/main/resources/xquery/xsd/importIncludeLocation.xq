(:
return list of schema location for import and include
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $import := $xmlSource//xsd:import
let $include := $xmlSource//xsd:include

return
<result>
{
  for $location in$import
  return
  <item>
    <type>import</type>
    <location>{string($location/@schemaLocation)}</location>
    <namespace>{string($location/@namespace)}</namespace>
  </item>
}
{
  for $location in $include
  return
  <item>
    <type>include</type>
    <location>{string($location/@schemaLocation)}</location>
    <namespace></namespace>
  </item>
}
</result>
