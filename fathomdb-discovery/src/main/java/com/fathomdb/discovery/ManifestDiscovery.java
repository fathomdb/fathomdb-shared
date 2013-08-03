//package com.fathomdb.discovery;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.io.StringReader;
//import java.lang.annotation.Annotation;
//import java.net.JarURLConnection;
//import java.net.URL;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Map;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import com.google.common.base.Charsets;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.io.CharStreams;
//import com.google.common.io.Closeables;
//import com.google.common.io.Files;
//
//public class ManifestDiscovery extends Discovery {
//	static final Logger log = LoggerFactory.getLogger(ManifestDiscovery.class);
//
//	private static DocumentBuilder buildDocumentBuilder(boolean namespaceAware) {
//		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//		docBuilderFactory.setNamespaceAware(namespaceAware);
//		DocumentBuilder docBuilder;
//		try {
//			docBuilder = docBuilderFactory.newDocumentBuilder();
//		} catch (ParserConfigurationException e) {
//			throw new IllegalStateException("Unable to create XML DOM", e);
//		}
//		return docBuilder;
//	}
//
//	public <T> Collection<Class> getSubTypesOf(Class<T> parent) {
//		ElementVisitor visitor = getElementVisitor();
//
//		String parentClassName = parent.getName();
//		List<String> subtypes = visitor.subtypes.get(parentClassName);
//
//		return loadClasses(subtypes);
//
//	}
//
//	ElementVisitor visitor;
//
//	private <T> ElementVisitor getElementVisitor() {
//		if (this.visitor == null) {
//			ElementVisitor visitor = new ElementVisitor();
//
//			List<Document> doms;
//			try {
//				doms = getDoms();
//			} catch (IOException e) {
//				throw new IllegalStateException("Error loading discovery resources", e);
//			}
//			for (Document dom : doms) {
//				visitor.walk(dom.getDocumentElement());
//			}
//			this.visitor = visitor;
//		}
//		return visitor;
//	}
//
//	@Override
//	public <T extends Annotation> Collection<Class> findAnnotatedClasses(Class<T> annotationClass) {
//		ElementVisitor visitor = getElementVisitor();
//
//		String annotationClassName = annotationClass.getName();
//		List<String> types = visitor.annotated.get(annotationClassName);
//
//		return loadClasses(types);
//	}
//
//	private Collection<Class> loadClasses(List<String> types) {
//		if (types == null) {
//			return Collections.emptyList();
//		}
//
//		List<Class> classes = Lists.newArrayList();
//		for (String type : types) {
//			try {
//				Class<?> clazz = Class.forName(type);
//				if (clazz == null) {
//					throw new IllegalStateException("Class.forName returned null for " + type);
//				}
//				classes.add(clazz);
//			} catch (ClassNotFoundException e) {
//				throw new IllegalStateException("Error loading class: " + type, e);
//			}
//		}
//		return classes;
//	}
//
//	static class ElementVisitor {
//		Map<String, List<String>> subtypes = Maps.newHashMap();
//		Map<String, List<String>> annotated = Maps.newHashMap();
//
//		void walk(Element element) {
//			if (!foundElement(element)) {
//				return;
//			}
//
//			NodeList children = element.getChildNodes();
//			for (int i = 0; i < children.getLength(); i++) {
//				Node node = children.item(i);
//				if (node.getNodeType() != Node.ELEMENT_NODE) {
//					continue;
//				}
//
//				Element el = (Element) node;
//				walk(el);
//			}
//		}
//
//		private boolean foundElement(Element element) {
//			String tagName = element.getTagName();
//			if ("SubTypesScanner".equals(tagName)) {
//				foundMap(element, subtypes);
//				return false;
//			}
//
//			if ("TypeAnnotationsScanner".equals(tagName)) {
//				foundMap(element, annotated);
//				return false;
//			}
//
//			return true;
//		}
//
//		private static void foundMap(Element parent, Map<String, List<String>> target) {
//			NodeList entries = parent.getChildNodes();
//			for (int i = 0; i < entries.getLength(); i++) {
//				Node node = entries.item(i);
//				if (node.getNodeType() != Node.ELEMENT_NODE) {
//					continue;
//				}
//
//				Element el = (Element) node;
//				if ("entry".equals(el.getTagName())) {
//					foundMapEntry(el, target);
//				}
//			}
//		}
//
//		private static void foundMapEntry(Element parent, Map<String, List<String>> target) {
//			String key = null;
//			List<String> values = null;
//
//			NodeList entries = parent.getChildNodes();
//			for (int i = 0; i < entries.getLength(); i++) {
//				Node node = entries.item(i);
//				if (node.getNodeType() != Node.ELEMENT_NODE) {
//					continue;
//				}
//
//				Element el = (Element) node;
//				if ("key".equals(el.getTagName())) {
//					key = getContents(el);
//				} else if ("values".equals(el.getTagName())) {
//					values = collectValues(el);
//				}
//			}
//
//			if (key == null || values == null) {
//				return;
//			}
//
//			if (target.containsKey(key)) {
//				values.addAll(target.get(key));
//			}
//
//			target.put(key, values);
//		}
//
//		private static String getContents(Element el) {
//			String text = el.getTextContent();
//			text = text.trim();
//			return text;
//		}
//
//		private static List<String> collectValues(Element parent) {
//			List<String> values = Lists.newArrayList();
//
//			NodeList entries = parent.getChildNodes();
//			for (int i = 0; i < entries.getLength(); i++) {
//				Node node = entries.item(i);
//				if (node.getNodeType() != Node.ELEMENT_NODE) {
//					continue;
//				}
//
//				Element el = (Element) node;
//				values.add(getContents(el));
//			}
//
//			return values;
//		}
//	}
//
//	private List<Document> getDoms() throws IOException {
//		List<URL> urls = Lists.newArrayList();
//		List<Document> doms = Lists.newArrayList();
//
//		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//		Enumeration<URL> resources = contextClassLoader.getResources("META-INF/reflections");
//		while (resources.hasMoreElements()) {
//			URL url = resources.nextElement();
//			urls.add(url);
//		}
//
//		for (URL url : urls) {
//			String protocol = url.getProtocol();
//
//			if ("file".equals(protocol)) {
//				String filePath = url.getFile();
//				if (filePath != null) {
//					File dir = new File(filePath);
//					if (!dir.exists()) {
//						continue;
//					}
//					for (File resourceFile : dir.listFiles()) {
//						if (!resourceFile.isFile()) {
//							continue;
//						}
//						try {
//							Document dom = loadResource(resourceFile);
//							doms.add(dom);
//						} catch (Exception e) {
//							throw new IllegalStateException("Error loading resource: " + resourceFile, e);
//						}
//					}
//				} else {
//					throw new IllegalStateException("No file for file URL: " + url);
//				}
//			} else if ("jar".equals(protocol)) {
//				try {
//					JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
//
//					JarFile jar = jarConnection.getJarFile();
//
//					Enumeration<JarEntry> entries = jar.entries();
//					while (entries.hasMoreElements()) {
//						JarEntry jarEntry = entries.nextElement();
//
//						if (jarEntry.isDirectory()) {
//							continue;
//						}
//
//						String name = jarEntry.getName();
//
//						if (!name.startsWith("META-INF/reflections/")) {
//							continue;
//						}
//
//						try {
//							Document dom = loadResource(jar, jarEntry);
//							doms.add(dom);
//						} catch (Exception e) {
//							throw new IllegalStateException("Error loading resource: " + jar.getName() + ":"
//									+ jarEntry.getName(), e);
//						}
//					}
//				} catch (IOException e) {
//					throw new IllegalStateException("Error reading entry: " + url, e);
//				}
//			} else {
//				throw new IllegalStateException("Unhandled protocol: " + protocol);
//			}
//		}
//
//		return doms;
//	}
//
//	private Document loadResource(JarFile jarFile, JarEntry jarEntry) throws IOException {
//		String xml;
//
//		Reader reader = new InputStreamReader(jarFile.getInputStream(jarEntry), Charsets.UTF_8);
//		try {
//			xml = CharStreams.toString(reader);
//		} finally {
//			Closeables.closeQuietly(reader);
//		}
//		try {
//			return buildDom(xml);
//		} catch (SAXException e) {
//			throw new IOException("Error reading resource: " + jarFile.getName() + ":" + jarEntry.getName(), e);
//		}
//
//	}
//
//	private Document loadResource(File file) throws IOException {
//		String xml = Files.toString(file, Charsets.UTF_8);
//
//		try {
//			return buildDom(xml);
//		} catch (SAXException e) {
//			throw new IOException("Error reading resource: " + file, e);
//		}
//
//	}
//
//	private Document buildDom(String xml) throws SAXException, IOException {
//		boolean namespaceAware = false;
//		DocumentBuilder docBuilder = buildDocumentBuilder(namespaceAware);
//		Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
//
//		// normalize text representation
//		doc.getDocumentElement().normalize();
//
//		return doc;
//	}
//
//	@Override
//	public List<Class> findClassesInPackage(Package package1) {
//		// TODO: This should be easy..
//		throw new UnsupportedOperationException();
//	}
//
// }
