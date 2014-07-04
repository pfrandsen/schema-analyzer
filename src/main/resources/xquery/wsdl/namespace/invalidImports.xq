(:
Find imported namespaces that are not in target namespace and not under "http://technical.schemas.nykreditnet.net/"
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $targetNS := string($xmlSource/wsdl:definitions/@targetNamespace)
let $invalidNamespaces := for $i in ($xmlSource//wsdl:import/@namespace, $xmlSource//xsd:import/@namespace)
  return
  	 if (not(matches(string($i), "^http://technical.schemas.nykreditnet.net/")) and not(matches(string($i), $targetNS))) then
  	   string($i)
  	 else
       ()

return
<result>
{for $invalidNamespace in distinct-values($invalidNamespaces)
  return
  <item>
    <namespace>{$invalidNamespace}</namespace>
  </item>
}
</result>
