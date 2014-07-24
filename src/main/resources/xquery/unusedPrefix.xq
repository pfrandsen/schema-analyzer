

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
let $unused := for $n in $ns return if ($n = $used) then () else $n


return
<result>
{for $a in $unused
  return
  <item>
    <prefix>{$a}</prefix>
    <namespace>{namespace-uri-for-prefix(string($a), $top)}</namespace>
  </item>
}
</result>
