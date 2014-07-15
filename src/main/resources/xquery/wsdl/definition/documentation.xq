(:
returns documentation for wsdl (top level documentation)
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $doc := $xmlSource/wsdl:definitions/normalize-space(concat( string-join( wsdl:documentation/text(), " "), " ",
  string-join( wsdl:documentation/wsdl:string/text(), " ")))

return
<result>
  <item>
    <documentation>{$doc}</documentation>
  </item>
</result>

