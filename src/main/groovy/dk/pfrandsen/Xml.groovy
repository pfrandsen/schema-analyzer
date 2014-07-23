package dk.pfrandsen

import groovy.xml.XmlUtil

class Xml {

    public static List<Map<String, String>> parseXQueryResult(String xml) {
        List<Map<String, String>> result = new ArrayList<>();

        def rootNode = new XmlSlurper().parseText(xml);
        rootNode.children().each { item ->
            Map<String, String> map = new HashMap<>()
            result.add(map)
            item.children().each { tag ->
                String key = tag.name()
                String value = ""
                if (tag.children().size() == 0) {
                    value = tag.text()
                } else {
                    tag.children().each {
                        String tmp = XmlUtil.serialize(it)
                        value += tmp.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
                    }
                }
                map.put(key, value)
            }
        }
        return result;
    }

    public static String asPretty(String xml) {
        return XmlUtil.serialize(xml)
    }
}
