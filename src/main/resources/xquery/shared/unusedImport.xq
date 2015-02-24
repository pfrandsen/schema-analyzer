

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare namespace wsdl="http://schemas.xmlsoap.org/wsdl/";
declare variable $xmlSource external;

let $top := if (exists($xmlSource/xsd:schema)) then $xmlSource/xsd:schema else $xmlSource/wsdl:definitions

(: find all the namespaces prefixes in scope of the top level node; remove implicit 'xml' namespace :)
let $topWsdl := if (exists($xmlSource/wsdl:definitions)) then in-scope-prefixes($xmlSource/wsdl:definitions) else ()
let $topXsd := if (exists($xmlSource/xsd:schema)) then in-scope-prefixes($xmlSource/xsd:schema) else ()
let $topNs := distinct-values(($topXsd, $topWsdl))
let $ns := for $x in $topNs return if ($x eq 'xml') then () else $x  (: remove the special implicit namespace 'xml' :)

(: find all the prefixes used in nodes :)
let $nodes := for $a in $xmlSource//node() return if (contains(name($a), ':'))
  then substring-before(name($a), ':') else ()
(: find all the prefixes used in attribute values :)
let $attrVal := for $a in $xmlSource/*//@* return if (contains($a, ':')) then substring-before(string($a), ':') else ()
(: find attributes with namespace prefix and return the prefixes :)
let $qualifiedAttr := for $a in $xmlSource/*//@* return if (contains(name($a), ':'))
  then substring-before(string(name($a)), ':') else ()

let $used := distinct-values(($nodes, $attrVal, $qualifiedAttr))
(: map the used prefixes to namespace uri's :)
let $usedUri := for $pre in $used return namespace-uri-for-prefix(string($pre), $top)
let $importedNamespaces := ($xmlSource//wsdl:import/@namespace, $xmlSource//xsd:import/@namespace)
(: find the imported namespaces not used :)
let $unused := for $n in $importedNamespaces return if ($n = $usedUri) then () else $n

return
<result>
{for $ns in $unused
  return
  <item>
    <namespace>{string($ns)}</namespace>
  </item>
}
</result>
