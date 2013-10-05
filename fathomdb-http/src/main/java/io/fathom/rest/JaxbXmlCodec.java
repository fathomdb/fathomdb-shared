package io.fathom.rest;

import io.fathom.xml.DomUtils;
import io.fathom.xml.JaxbHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;

import com.google.common.base.Charsets;

public class JaxbXmlCodec /* extends XmlCodec */{
	public static String toXml(Object object, boolean formatted) throws JAXBException {
		String content = JaxbHelper.toXml(object, formatted);
		return content;
	}

	public static <T> T deserializeXmlObject(Reader reader, Class<T> clazz, boolean recordXml) throws JAXBException {
		return JaxbHelper.deserializeXmlObject(reader, clazz, recordXml);
	}

	public static <T> T deserializeXmlObject(InputStream is, Class<T> clazz, boolean recordXml) throws JAXBException {
		Reader reader = new InputStreamReader(is, Charsets.UTF_8);
		return deserializeXmlObject(reader, clazz, recordXml);
	}

	public static <T> T deserializeXmlObject(Node node, Class<T> clazz, boolean recordXml) throws JAXBException {
		String xml = DomUtils.toXml(node);
		return deserializeXmlObject(new StringReader(xml), clazz, recordXml);
	}
}
