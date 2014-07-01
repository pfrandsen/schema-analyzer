(:
Extracts the <message>'s name in definitions/message
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $messages := $xmlSource/wsdl:definitions/wsdl:message

return
<result>
{for $message in $messages
  return
  <item>
    <name>{string($message/@name)}</name>
  </item>
}
</result>
