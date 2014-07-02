(:
Extracts list of faults for an <operation> for a portType in definitions/portType

<result>
  <item>
    <name>fault name</name>
    <message>fault message</message>
    <message-local>message local name</message-local>
    <message-ns>message namespace uri</message-ns>
    <message-pre>message namespace prefix</message-pre>
  </item>
  <item>
    <name>fault name</name>
    <message>fault message</message>
    <message-local>message local name</message-local>
    <message-ns>message namespace uri</message-ns>
    <message-pre>message namespace prefix</message-pre>
  </item>
  ...
</result>

:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;
declare variable $name external;
declare variable $name2 external;

let $faults := $xmlSource/wsdl:definitions/wsdl:portType[@name=$name]/wsdl:operation[@name=$name2]/wsdl:fault

return
<result>
{for $fault in $faults
  let $fault_msg := $fault/@message
  let $message_qn := resolve-QName($fault_msg, $fault)
  let $message_local := if (exists($message_qn)) then local-name-from-QName($message_qn) else ''
  let $message_ns := if (exists($message_qn)) then namespace-uri-from-QName($message_qn) else ''
  let $message_pre := if (exists($message_qn)) then prefix-from-QName($message_qn) else ''
  return
  <item>
    <name>{string($fault/@name)}</name>
    <message>{string($fault_msg)}</message>
    <message-local>{$message_local}</message-local>
    <message-ns>{$message_ns}</message-ns>
    <message-pre>{$message_pre}</message-pre>
  </item>
}
</result>
