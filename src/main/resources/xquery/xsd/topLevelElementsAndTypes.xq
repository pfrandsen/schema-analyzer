(:
returns information on each top-level type and element defined in schema
:)

declare namespace xsd="http://www.w3.org/2001/XMLSchema";
declare variable $xmlSource external;

let $top := ($xmlSource/xsd:schema/xsd:simpleType, $xmlSource/xsd:schema/xsd:complexType, $xmlSource//xsd:element)

return
    <result>
        {
            for $t in $top
            return
                <item>
                    <name>{string($t/@name)}</name>
                    <node>{local-name($t)}</node>
                </item>
        }
    </result>
