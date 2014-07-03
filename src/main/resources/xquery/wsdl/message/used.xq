(:
Extracts the messages that are used in portType and binding
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $messages := for $m in ($xmlSource/wsdl:definitions/wsdl:portType//@message,
  $xmlSource/wsdl:definitions/wsdl:binding//@message)
  return string($m)

return
<result>
{for $message in distinct-values($messages)
  let $msg_local := if (contains($message, ':')) then substring-after($message, ':') else $message
  let $msg_pre := if (contains($message, ':')) then substring-before($message, ':') else ''
  return
  <item>
    <message>{$message}</message>
    <msg-local>{$msg_local}</msg-local>
    <msg-prefix>{$msg_pre}</msg-prefix>
  </item>
}
</result>
