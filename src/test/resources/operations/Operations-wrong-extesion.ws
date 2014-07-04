<?xml version="1.0" encoding="UTF-8"?>
<wsdl:definitions name="Entity"
                  targetNamespace="http://service.schemas/domain/service/v1" 
                  xmlns:tns="http://service.schemas/domain/service/v1" 
                  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
                  xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" 
                  xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/">

    <wsdl:portType name="EntityServiceThree">
        <wsdl:operation name="putEntity">
            <wsdl:input message="msg"/>
            <wsdl:output message="msg"/>
        </wsdl:operation>

</wsdl:definitions>
