/*
 * Copyright 2005-2015 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.pfry.route;

import javax.inject.Inject;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.Uri;
import org.apache.camel.component.cxf.common.message.CxfConstants;

import io.fabric8.annotations.Alias;
import io.fabric8.annotations.ServiceName;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName("helloCamel")
public class HelloRoute extends RouteBuilder {
	
    private static final String OPERATION_NAMESPACE = "http://apache.org/hello2_world_soap_http";
	private static final String OPERATION_NAME = "greetMe";
	
	@Inject
    @Uri("timer:foo?period=5000")
    private Endpoint inputEndpoint;	
	
    @Inject
    @ServiceName("broker-amq-tcp")
    @Alias("jms")
    ActiveMQComponent activeMQComponent;	

    @Override
    public void configure() throws Exception {
    	
		Processor convertToSoapMessageProcessor = new ConvertToSoapMessageProcessor();

	     from("cxf://http://0.0.0.0:8080?wsdlURL=wsdl/helloworld.wsdl&dataFormat=PAYLOAD").id("helloRoute")
	    .convertBodyTo(String.class)
	    //.to("validator:schema/hello.xsd")
	    .to("xslt:xslt/mappingRequest.xsl")
	    .log("After XSL")
	    .to("log:org.pfry.route?level=INFO")
	    .setHeader(CxfConstants.OPERATION_NAMESPACE, constant(OPERATION_NAMESPACE))
	    .setHeader(CxfConstants.OPERATION_NAME, constant(OPERATION_NAME))
	    .wireTap("direct:fileAudit")
	    .to("cxf://http://{{service:qs-cdi-camel-mock}}/hello2ws?wsdlURL=wsdl/helloworld2.wsdl&dataFormat=PAYLOAD")
	    //.to("cxf://http://0.0.0.0:9091/hello2ws?wsdlURL=wsdl/helloworld2.wsdl&dataFormat=PAYLOAD")
	    .log("after webservice call, output is ${in.body}")
	    .to("xslt:xslt/mappingResponse.xsl")
	    .wireTap("direct:jmsAudit")
	    .process(convertToSoapMessageProcessor)
	    //.to("validator:wsdl/CombinedSchema.xsd") NO RESPONSE XSD
	    .to("log:org.pfry.route?level=INFO");
	     
	     from (inputEndpoint)
	    .to("language:constant:resource:classpath:/hello.xml")
	    .to("cxf://http://127.0.0.1:8080?wsdlURL=wsdl/helloworld.wsdl&dataFormat=MESSAGE");
	     
	     from("direct:fileAudit")
	    .to("file:target/audit");
	     
	     from("direct:jmsAudit")
	    .to("jms:queue:TEST.FOO");
    }

}
