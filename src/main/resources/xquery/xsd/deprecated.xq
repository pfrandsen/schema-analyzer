(:
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $tns := string($xmlSource/xsd:schema/@targetNamespace)
let $deprecated := $xmlSource//xsd:documentation[contains(lower-case(.), "deprecated")]
let $ignore := compare($tns, "http://concept.schemas.nykreditnet.net/enterprise/person/danmark/v1") eq 0
                or compare($tns, "http://concept.schemas.nykreditnet.net/enterprise/virksomhed/danmark/v1") eq 0

return
<result>
{
  if (empty($deprecated) or $ignore) then () else
  <item>
    <namespace>{$tns}</namespace>
  </item>
}
</result>
