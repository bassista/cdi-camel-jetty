package org.pfry.route;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ConvertToSoapMessageProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		String body = (String) exchange.getIn().getBody();
		InputStream in = new ByteArrayInputStream(body.getBytes());
		MessageFactory mf = MessageFactory.newInstance();
		exchange.getIn().setBody(mf.createMessage(null, in));
	}

}
