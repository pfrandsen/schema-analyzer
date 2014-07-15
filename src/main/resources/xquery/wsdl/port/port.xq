(:
Extracts the <port>'s name and the enclosing service name in definitions/service/port

<result>
  <item>
    <name>port name</name>
    <service>service name</service>
    <binding-local>binding local name</binding-local>
    <binding-ns>binding namespace</binding-ns>
    <binding-pre>binding prefix</binding-pre>
  </item>
  <item>
    <name>port name</name>
    <service>service name</service>
    <binding-local>binding local name</binding-local>
    <binding-ns>binding namespace</binding-ns>
    <binding-pre>binding prefix</binding-pre>
  </item>
  ....
</result>
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $ports := $xmlSource/wsdl:definitions/wsdl:service/wsdl:port

return
<result>
{for $port in $ports
  let $service := $xmlSource/wsdl:definitions/wsdl:service[wsdl:port/@name = $port/@name]
  let $binding_qn := resolve-QName($port/@binding, $port)
  let $binding_local := if (exists($binding_qn)) then local-name-from-QName($binding_qn) else ''
  let $binding_ns := if (exists($binding_qn)) then namespace-uri-from-QName($binding_qn) else ''
  let $binding_pre := if (exists($binding_qn)) then prefix-from-QName($binding_qn) else ''
  return
  <item>
    <name>{string($port/@name)}</name>
    <service>{string($service/@name)}</service>
    <binding-local>{$binding_local}</binding-local>
    <binding-ns>{$binding_ns}</binding-ns>
    <binding-pre>{$binding_pre}</binding-pre>
  </item>
}
</result>


(:
The parent ref below fails - report error to EZH
return
<result>
{for $port in $ports
  return
  <item>
    <name>{string($port/@name)}</name>
    <service>{string($port/../@name)}</service>
  </item>
}
</result>
:)