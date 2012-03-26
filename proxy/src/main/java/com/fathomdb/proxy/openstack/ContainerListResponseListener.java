package com.fathomdb.proxy.openstack;

import java.io.StringReader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.openstack.fs.OpenstackFile;

public class ContainerListResponseListener extends RestResponseListener {

	private List<OpenstackFile> items;

	public ContainerListResponseListener(Channel channel) {
		super(channel);
	}

	enum State {
		ROOT, CONTAINER, OBJECT, OBJECT_NAME, OBJECT_HASH, OBJECT_BYTES, OBJECT_CONTENT_TYPE, OBJECT_LAST_MODIFIED;
	}

	@Override
	protected void gotResponse(HttpResponse response, String data)
			throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlReader = inputFactory
				.createXMLStreamReader(new StringReader(data));

		// <container name="test_container_1">
		// <object>
		// <name>test_object_1</name>
		// <hash>4281c348eaf83e70ddce0e07221c3d28</hash>
		// <bytes>14</bytes>
		// <content_type>application/octet-stream</content_type>
		// <last_modified>2009-02-03T05:26:32.612278</last_modified>
		// </object>
		// <object>
		// <name>test_object_2</name>
		// <hash>b039efe731ad111bc1b0ef221c3849d0</hash>
		// <bytes>64</bytes>
		// <content_type>application/octet-stream</content_type>
		// <last_modified>2009-02-03T05:26:32.612278</last_modified>
		// </object>
		// </container>

		State state = State.ROOT;

		StringBuilder sb = null;

		long objectBytes = -1;
		String objectName = null;
		String objectHash = null;
		String objectContentType = null;
		String objectLastModified = null;

		while (xmlReader.hasNext()) {
			int eventType = xmlReader.next();
			switch (eventType) {
			case XMLEvent.START_ELEMENT: {
				QName name = xmlReader.getName();
				String elementName = name.getLocalPart();

				switch (state) {
				case ROOT: {
					if (elementName.equals("container")) {
						state = State.CONTAINER;
						continue;
					}
					break;
				}

				case CONTAINER: {
					if (elementName.equals("object")) {
						state = State.OBJECT;
						continue;
					}
					break;
				}

				case OBJECT: {
					sb = new StringBuilder();
					objectBytes = -1;
					objectName = null;
					objectHash = null;
					objectContentType = null;
					objectLastModified = null;

					if (elementName.equals("name")) {
						state = State.OBJECT_NAME;
						continue;
					}
					if (elementName.equals("hash")) {
						state = State.OBJECT_HASH;
						continue;
					}
					if (elementName.equals("bytes")) {
						state = State.OBJECT_BYTES;
						continue;
					}
					if (elementName.equals("content_type")) {
						state = State.OBJECT_CONTENT_TYPE;
						continue;
					}
					if (elementName.equals("last_modified")) {
						state = State.OBJECT_LAST_MODIFIED;
						continue;
					}

					break;
				}
				}

				throw new IllegalArgumentException("Unexpected element: "
						+ elementName);
				// break;
			}

			case XMLEvent.CHARACTERS: {
				if (sb != null) {
					sb.append(xmlReader.getText());
				}
				break;
			}

			case XMLEvent.END_ELEMENT: {
				switch (state) {
				case OBJECT_BYTES: {
					objectBytes = Long.parseLong(sb.toString());
					state = State.OBJECT;
					break;
				}
				case OBJECT_NAME: {
					objectName = sb.toString();
					state = State.OBJECT;
					break;
				}
				case OBJECT_HASH: {
					// TODO: From hex?
					objectHash = sb.toString();
					state = State.OBJECT;
					break;
				}
				case OBJECT_CONTENT_TYPE: {
					// TODO: Share strings?
					objectContentType = sb.toString();
					state = State.OBJECT;
					break;
				}
				case OBJECT_LAST_MODIFIED: {
					// TODO: parse?
					objectLastModified = sb.toString();
					state = State.OBJECT;
					break;
				}
				case OBJECT: {
					OpenstackFile item = new OpenstackFile(objectName,
							objectHash, objectBytes, objectContentType,
							objectLastModified);
					items.add(item);
					state = State.CONTAINER;
					break;
				}
				case CONTAINER: {
					state = State.ROOT;
					break;
				}
				}
				sb = null;

				break;
			}
			// default:
			// throw new IllegalStateException("Unknown XML node type: " +
			// eventType);
			}
		}

		xmlReader.close();
	}

}
