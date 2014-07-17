(:
returns information on named enumerations defined in schema but not used in element in the schema itself
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $tns := string($xmlSource/xsd:schema/@targetNamespace)
let $enums := $xmlSource/xsd:schema/xsd:simpleType[/xsd:restriction/xsd:enumeration]
let $elements :=  $xmlSource//xsd:element[exists(@type)]

(: construct namespace qualified type name for all types used in elements:)
let $elementTypes := for $element in $elements return
   concat(string(namespace-uri-from-QName(resolve-QName($element/@type, $element))), ':',
          string(local-name-from-QName(resolve-QName($element/@type, $element))))

(: find the enumerations not used (if any) :)
let $unused := for $x in $enums return
   if (exists($elementTypes[. = (concat($tns, ':', string($x/@name)))])) then () else $x

return
<result>
{
  for $x in $unused
  return
  <item>
    <name>{string($x/@name)}</name>
  </item>
}
</result>
