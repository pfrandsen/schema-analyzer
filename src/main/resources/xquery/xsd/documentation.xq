(:
returns documentation for each element in schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $elements := $xmlSource/xsd:schema/xsd:element

return
<result>
{
  for $element in $elements
    let $doc := $element/xsd:annotation/xsd:documentation/normalize-space(concat(string-join(text(), " "), " ",
                string-join(xsd:string/text(), " ")))
  return
  <item>
    <name>{string($element/@name)}</name>
    <documentation>{$doc}</documentation>
  </item>
}
</result>
