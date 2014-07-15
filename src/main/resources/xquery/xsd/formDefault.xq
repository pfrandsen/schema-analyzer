(:
Extracts the attribute and element form defaults from the schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $element := $xmlSource/xsd:schema/@elementFormDefault
let $attribute := $xmlSource/xsd:schema/@attributeFormDefault

return
<result>
  <item>
    <element>{string($element)}</element>
    <attribute>{string($attribute)}</attribute>
  </item>
</result>
