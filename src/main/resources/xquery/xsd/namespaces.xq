(:
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $schema := $xmlSource/xsd:schema
let $prefixes := in-scope-prefixes($schema)
let $prefix_ns := for $prefix in $prefixes return string(namespace-uri-for-prefix($prefix, $schema))

let $ns := distinct-values(($xmlSource//xsd:import/string(@namespace), $xmlSource//xsd:include/string(@namespace),
                          $xmlSource//node()/namespace-uri(.), string($xmlSource/xsd:schema/@targetNamespace),
                          $prefix_ns))
(:
info: distinct-values does not currently work in XQuery processor
let $ns_ordered := for $n in distinct-values($ns) order by $n return $n
:)

(: remove empty namespaces :)
let $namespaces := for $n in $ns return if ($n = "") then () else $n

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
