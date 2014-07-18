(:
returns information on elements defined directly under xsd:schema that are not allowed:
elements with type that are not in the target namespace of the element itself or in a concept namespace
unconstrained elements (elements with no type attribute and no child nodes except annotation
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $tns := $xmlSource/xsd:schema/@targetNamespace

(: find all top-level elements with type attribute :)
let $allTyped := $xmlSource/xsd:schema/xsd:element[exists(@type)]
(: find all top-level elements without type attribute :)
let $allUntyped := $xmlSource/xsd:schema/xsd:element[not(exists(@type))]

(: filter untyped elements to find untyped with no immediate children except annotations :)
let $unconstrained := for $x in $allUntyped return if (count($x/*) eq count($x/xsd:annotation)) then $x else ()

(: filter typed elements to find types that are not in the target namespace or in a concept namespace :)
let $illegalNamespace := for $x in $allTyped return
   if (string($tns) eq string(namespace-uri-from-QName(resolve-QName(string($x/@type), $x)))
       or contains(string(namespace-uri-from-QName(resolve-QName(string($x/@type), $x))), '/concept')) then () else $x

return
<result>
{
  for $node in $illegalNamespace
    let $ns := string(namespace-uri-from-QName(resolve-QName(string($node/@type), $node)))
  return
  <item>
    <name>{string($node/@name)}</name>
    <message>Illegal namespace '{$ns}'</message>
  </item>
}
{
  for $node in $unconstrained
  return
  <item>
    <name>{string($node/@name)}</name>
    <message>Unconstrained without content</message>
  </item>
}
</result>
