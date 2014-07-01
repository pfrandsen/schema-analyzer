(:
Extracts details for a <message> by name. A message can consist of multiple parts
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;
declare variable $name external;

let $parts := $xmlSource/wsdl:definitions/wsdl:message[@name=$name]/wsdl:part

return
<result>
{for $part in $parts
  let $elem_qn := resolve-QName($part/@element, $part)
  let $elem_local := if (exists($elem_qn)) then local-name-from-QName($elem_qn) else ''
  let $elem_ns := if (exists($elem_qn)) then namespace-uri-from-QName($elem_qn) else ''
  let $elem_pre := if (exists($elem_qn)) then prefix-from-QName($elem_qn) else ''
  let $type_qn := resolve-QName($part/@type, $part)
  let $type_local := if (exists($type_qn)) then local-name-from-QName($type_qn) else ''
  let $type_ns := if (exists($type_qn)) then namespace-uri-from-QName($type_qn) else ''
  let $type_pre := if (exists($type_qn)) then prefix-from-QName($type_qn) else ''
  return
  <item>
    <name>{string($part/@name)}</name>
    <element-local>{$elem_local}</element-local>
    <element-namespace>{$elem_ns}</element-namespace>
    <element-prefix>{$elem_pre}</element-prefix>
    <type-local>{$type_local}</type-local>
    <type-namespace>{$type_ns}</type-namespace>
    <type-prefix>{$type_pre}</type-prefix>
  </item>
}
</result>
