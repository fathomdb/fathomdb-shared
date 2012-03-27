package com.fathomdb.proxy.openstack;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class KeystoneResponseListener extends RestResponseListener {
	static final String XML_NS = "http://docs.openstack.org/identity/api/v2.0";

	private URI swiftUrl;

	final List<String> tokenIds = new ArrayList<String>();
	final List<Endpoint> endpoints = new ArrayList<Endpoint>();

	static class Endpoint {
		public String region;
		public URI publicUrl;

		@Override
		public String toString() {
			return "Endpoint [region=" + region + ", publicUrl=" + publicUrl
					+ "]";
		}

	}

	public KeystoneResponseListener(Channel channel) {
		super(channel);
	}

	@Override
	protected void gotResponse(HttpResponse response, String data)
			throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlReader = inputFactory
				.createXMLStreamReader(new StringReader(data));

		String currentServiceType = null;

		while (xmlReader.hasNext()) {
			int eventType = xmlReader.next();
			switch (eventType) {
			case XMLEvent.START_ELEMENT:
				QName name = xmlReader.getName();
				String elementName = name.getLocalPart();
				if (elementName.equals("token")) {
					tokenIds.add(xmlReader.getAttributeValue(null, "id"));
				}

				if (elementName.equals("service")) {
					currentServiceType = xmlReader.getAttributeValue(null,
							"type");
				}

				if (elementName.equals("endpoint")) {
					if (currentServiceType.equals("object-store")) {
						Endpoint endpoint = new Endpoint();
						endpoint.region = xmlReader.getAttributeValue(null,
								"region");
						String publicUrl = xmlReader.getAttributeValue(null,
								"publicURL");
						endpoint.publicUrl = new URI(publicUrl);
						endpoints.add(endpoint);
					}
				}
				// System.out.print(" " + xmlReader.getName() + " ");
				break;
			case XMLEvent.CHARACTERS:
				// System.out.print(" " + xmlReader.getText() + " ");
				break;
			// case XMLEvent.ATTRIBUTE:
			// System.out.print(" " + xmlReader.getName() + " ");
			// break;
			case XMLEvent.END_ELEMENT:
				// System.out.print(" " + xmlReader.getName() + " ");
				break;
			// default:
			// throw new IllegalStateException("Unknown XML node type: " +
			// eventType);
			}
		}

		xmlReader.close();

		Endpoint best = null;
		for (Endpoint endpoint : endpoints) {
			if (best == null) {
				best = endpoint;
				continue;
			}

			 if (best.publicUrl.getHost().contains("cdn1.clouddrive.com")) {
				best = endpoint;
				continue;
			}
		}

		if (best == null) {
			throw new IllegalArgumentException("Swift endpoint not found");
		}

		swiftUrl = best.publicUrl;

	}

	public URI getSwiftUrl() {
		return swiftUrl;
	}

	public boolean isAuthenticated() {
		return !tokenIds.isEmpty();
	}

	public String getTokenId() {
		if (tokenIds.size() != 1)
			throw new IllegalStateException();
		return tokenIds.get(0);
	}

}
