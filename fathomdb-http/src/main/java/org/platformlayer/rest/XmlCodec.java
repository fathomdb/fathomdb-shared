//package org.platformlayer.rest;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.StringReader;
//
//import javax.xml.bind.JAXBException;
//
//import org.platformlayer.xml.DomUtils;
//import org.w3c.dom.Node;
//
//import com.google.common.base.Charsets;
//
//public abstract class XmlCodec {
//	public abstract String toXml(Object object, boolean formatted) throws Exception;
//
//	public abstract <T> T deserializeXmlObject(Reader reader, Class<T> clazz, boolean recordXml) throws JAXBException;
//
//	public <T> T deserializeXmlObject(InputStream is, Class<T> clazz, boolean recordXml) throws JAXBException {
//		Reader reader = new InputStreamReader(is, Charsets.UTF_8);
//		return deserializeXmlObject(reader, clazz, recordXml);
//	}
//
//	public <T> T deserializeXmlObject(Node node, Class<T> clazz, boolean recordXml) throws JAXBException {
//		String xml = DomUtils.toXml(node);
//		return deserializeXmlObject(new StringReader(xml), clazz, recordXml);
//	}
//
// }
