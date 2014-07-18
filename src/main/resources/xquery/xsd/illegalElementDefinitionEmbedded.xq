(:
returns information on embedded elements (elements that are not directly under xsd:schema) that are not allowed:
elements with type that are not in the target namespace of the element itself or in a concept namespace
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $tns := string($xmlSource/xsd:schema/@targetNamespace)

(: find all top-level nodes that have embedded element(s) :)
let $allWithEmbedded := $xmlSource/xsd:schema/*[count(//xsd:element) ne 0]

let $allEmbedded := $xmlSource/xsd:schema/*//xsd:element

return
<result>
{
  for $node in $allWithEmbedded
      (: name of top level node :)
      let $nodeName := if (exists($node/@name)) then string($node/@name) else '(anonymous)'
      (: find all the child xsd:element's with invalid namespace :)
      let $invalidNS := for $child in $node//xsd:element return
        if (exists($child/@ref)) then
          let $tmp := string(namespace-uri-from-QName(resolve-QName(string($child/@ref), $child)))
          return if (($tns eq $tmp)  or contains($tmp, '/concept')) then () else $child
        else if (exists($child/@type)) then
          let $tmp := string(namespace-uri-from-QName(resolve-QName(string($child/@type), $child)))
          return if (($tns eq $tmp)  or contains($tmp, '/concept')) then () else $child
        else ()
      (: for each invalid child xsd:element compute the name and namespace and return item with these and information
         about the top-level parent node (just below xsd:schema) :)
      for $invalid in $invalidNS
        let $nameOfInvalid := if (exists($invalid/@name)) then string($invalid/@name) else if (exists($invalid/@ref))
          then string(local-name-from-QName(resolve-QName(string($invalid/@ref), $invalid))) else '(anonymous)'
        let $nsOfInvalid := if (exists($invalid/@type))
          then string(namespace-uri-from-QName(resolve-QName(string($invalid/@type), $invalid)))
          else if (exists($invalid/@ref))
          then string(namespace-uri-from-QName(resolve-QName(string($invalid/@ref), $invalid)))
          else ''
  return
  <item>
    <node>{string(local-name($node))}:{$nodeName}</node>
    <element>{$nameOfInvalid}</element>
    <message>Illegal namespace '{$nsOfInvalid}'</message>
  </item>
}
</result>
