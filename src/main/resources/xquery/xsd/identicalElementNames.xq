(:
return list of nodes with more than one immediate child xsd:element nodes where the element names are not unique

each item in the list contains the name and node-type for the top-level node where the elements were found +
the node-type of the elements immediate parent, a string containing all the element names separated by ',',
and a string containing the repeated element names separated by ','
Example:
<result>
  <item>
    <name>NodeOne</name>
    <node>element</node>
    <local-node>choice</local-node>
    <elements>Name1,Name2,Name3,Name3,Name1</elements>
    <repeated>Name1,Name3</repeated>
  </item>
  <item>
    <name>NewType</name>
    <node>complexType</node>
    <local-node>sequence</local-node>
    <elements>Name1,Name2,Name3,Name4,Name1</elements>
    <repeated>Name1</repeated>
  </item>
</result>

:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

(: find all top-level nodes with at least 2 elements in subtree - if there is less than two elements there
   can be no identical names :)
let $topNodes := $xmlSource/xsd:schema/*[count(//xsd:element) gt 1]

return
<result>
{
  for $node in $topNodes
  (: find all the nodes with at least two elements as immediate children :)
  let $top := if (count($node/xsd:element) gt 1) then $node else () (: check if top node have multiple elements :)
  let $embedded := $node//node()[count(/xsd:element) gt 1] (: select embedded nodes with multiple elements :)
  let $all :=  ($top, $embedded)
  for $x in $all
    (: determine element names from @name or @ref attribute :)
    let $names := for $n in $x/xsd:element return
      if (exists($n/@name)) then string($n/@name) else if (exists($n/@ref))
                then string(local-name-from-QName(resolve-QName(string($n/@ref), $n))) else '(anonymous)'
    (: find the element names that are repeated :)
    let $repeated := for $name in distinct-values($names) return
      if (count($names[.=$name]) gt 1) then $name else ()
  (: only return item if names are not unique :)
  return if (count($names) eq count(distinct-values($names))) then () else
  <item>
    <name>{string($node/@name)}</name>
    <node>{local-name($node)}</node>
    <local-node>{local-name($x)}</local-node>
    <elements>{string-join($names, ',')}</elements>
    <repeated>{string-join($repeated, ',')}</repeated>
  </item>
}
</result>
