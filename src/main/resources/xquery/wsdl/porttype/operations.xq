(:
Extracts the <operation>'s for a portType in definitions/portType
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;
declare variable $name external;

let $operations := $xmlSource/wsdl:definitions/wsdl:portType[@name=$name]/wsdl:operation

return
<result>
{for $operation in $operations
  let $input_msg := $operation/wsdl:input/@message
  let $input_qn := resolve-QName($input_msg, $operation)
  let $input_local := if (exists($input_qn)) then local-name-from-QName($input_qn) else ''
  let $input_ns := if (exists($input_qn)) then namespace-uri-from-QName($input_qn) else ''
  let $input_pre := if (exists($input_qn)) then prefix-from-QName($input_qn) else ''
  let $output_msg := $operation/wsdl:output/@message
  let $output_qn := resolve-QName($output_msg, $operation)
  let $output_local := if (exists($output_qn)) then local-name-from-QName($output_qn) else ''
  let $output_ns := if (exists($output_qn)) then namespace-uri-from-QName($output_qn) else ''
  let $output_pre := if (exists($output_qn)) then prefix-from-QName($output_qn) else ''
  return
  <item>
    <name>{string($operation/@name)}</name>
    <documentation>{$operation/wsdl:documentation}</documentation>
    <input>{string($input_msg)}</input>
    <input-local>{$input_local}</input-local>
    <input-ns>{$input_ns}</input-ns>
    <input-pre>{$input_pre}</input-pre>
    <output>{string($output_msg)}</output>
    <output-local>{$output_local}</output-local>
    <output-ns>{$output_ns}</output-ns>
    <output-pre>{$output_pre}</output-pre>
  </item>
}
</result>
