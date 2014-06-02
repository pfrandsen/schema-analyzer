Schema analyzer
===============

A set of tools to analyze schemas (xsd and wsdl) and report errors and warning according to a set of
compliance rules.

Miscellaneous
-------------

Add lib/* and libwsi-1-1/* to classpath

*lib*: Contains SOA Model (http://www.membrane-soa.org/downloads/) which does not seem to be available in Maven. SOA
Model is a Java API for WSDL and XML Schema.

*libwsi-1-1*: Contains libs for WS-I basic profile 1.1 conformance checking (from ws-i.org)

Clone: git clone git://github.com/pfrandsen/schema-analyzer.git