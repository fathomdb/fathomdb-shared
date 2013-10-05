package io.fathom.xml;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DomUtils {

	public static String toXml(Node node) {
		Source src = new DOMSource(node);
		return toXml(src, -1);
	}

	public static String toXml(XMLStreamReader xml) {
		Source src = new StAXSource(xml);
		return toXml(src, -1);
	}

	public static String toXml(Document xmlDocument) {
		return toXml(xmlDocument.getDocumentElement());
	}

	private static Transformer buildXmlTransformer() throws TransformerFactoryConfigurationError,
			TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		return transformer;
	}

	public static String toXml(Source src, int indent) {
		try {
			Transformer transformer = buildXmlTransformer();

			if (indent >= 0) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
			}

			StringWriter writer = new StringWriter();

			Result dest = new StreamResult(writer);
			transformer.transform(src, dest);

			return writer.getBuffer().toString();
		} catch (TransformerException e) {
			throw new IllegalArgumentException("Error transforming to XML", e);
		}
	}

}
