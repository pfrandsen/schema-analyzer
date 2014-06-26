

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

(: Find all imported namespaces; wsdl imports and xsd imports :)
let $importedNamespaces := ($xmlSource//wsdl:import/@namespace, $xmlSource//xsd:import/@namespace)

(: Find all used namespaces, :)
let $used := ($xmlSource//wsdl:message//@element, $xmlSource//wsdl:message//@type, $xmlSource//wsdl:binding/@type)
let $usedNamespaces := distinct-values(
	for $element in $used
		return namespace-uri-for-prefix(substring-before($element, ":"), $xmlSource/wsdl:definitions)
  )

(: Find imported namespaces not used :)
let $unusedNamespaces :=
	for $ns in $importedNamespaces
		return
			if (empty(index-of($usedNamespaces, $ns))) then
				$ns
			else
				()

return
<result>
{for $unusedNamespace in $unusedNamespaces
  return
  <item>
    <namespace>{string($unusedNamespace)}</namespace>
  </item>
}
</result>
