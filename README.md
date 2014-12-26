Schema analyzer
===============

A set of tools to analyze schemas (xsd and wsdl) and report errors and warning according to a set of compliance rules.

Classpath
---------

Add lib/soa-model* and lib/mxquery.jar to classpath

### lib folder contents
* [SOA Model](http://www.membrane-soa.org/downloads/) which does not seem to be available in Maven. SOA
Model is a Java API for WSDL and XML Schema.
* [MXQuery](http://mxquery.org/) A lightweight, full-featured XQuery Engine
* wsi-checker*.jar: Library for WS-I basic profile 1.1 conformance checking (from ws-i.org),
 see [wsi-checker](https://github.com/pfrandsen/wsi-checker).

Miscellaneous
-------------
Clone: git clone git://github.com/pfrandsen/schema-analyzer.git

TODO
----

Remove dependency on SOA model - use XQuery instead