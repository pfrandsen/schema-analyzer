(:
returns documentation for each operation in wsdl
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;


let $portTypes := $xmlSource/wsdl:definitions/wsdl:portType

return
<result>
{
  for $portType in $portTypes
    for $operation in $portType/wsdl:operation
    let $doc := $operation/normalize-space(concat( string-join( wsdl:documentation/text(), " "), " ",
                string-join( wsdl:documentation/wsdl:string/text(), " ")))
  return
  <item>
    <name>{string($operation/@name)}</name>
    <documentation>{$doc}</documentation>
    <portType>{string($portType/@name)}</portType>
  </item>
}
</result>

