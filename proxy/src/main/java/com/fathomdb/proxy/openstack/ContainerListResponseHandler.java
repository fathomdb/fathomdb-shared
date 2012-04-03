package com.fathomdb.proxy.openstack;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Date;

import com.fathomdb.proxy.http.Dates;
import com.fathomdb.proxy.utils.Hex;

public class ContainerListResponseHandler extends StreamingRestResponseHandler {
	final StreamingJsonParser parser;
	final ObjectListJsonHandler handler;
	final ObjectMetadataListener listener;

	Object result;

	public ContainerListResponseHandler(ObjectMetadataListener listener) {
		this.listener = listener;

		handler = new ObjectListJsonHandler();
		StreamingJsonTokenizer tokenizer = new StreamingJsonTokenizer();
		parser = new StreamingJsonParser(tokenizer, handler);
	}

	enum State {
		ROOT, CONTAINER, OBJECT, OBJECT_NAME, OBJECT_HASH, OBJECT_BYTES, OBJECT_CONTENT_TYPE, OBJECT_LAST_MODIFIED;
	}

	class ObjectListJsonHandler implements JsonHandler {
		State state = State.ROOT;

		long objectBytes = -1;
		String objectName = null;
		byte[] objectHash = null;
		String objectContentType = null;
		long objectLastModified = 0;

		@Override
		public void endArray() {
			switch (state) {
			case CONTAINER: {
				state = State.ROOT;
				break;
			}
			default: {
				throw new IllegalArgumentException();
			}
			}
		}

		@Override
		public void beginObject() {
			switch (state) {

			case CONTAINER: {
				state = State.OBJECT;

				objectBytes = -1;
				objectName = null;
				objectHash = null;
				objectContentType = null;
				objectLastModified = 0;
				break;
			}

			default: {
				throw new IllegalArgumentException();
			}
			}
		}

		@Override
		public void gotValue(ValueType type, String value) {
			switch (state) {
			case OBJECT_BYTES: {
				objectBytes = Long.parseLong(value);
				state = State.OBJECT;
				break;
			}
			case OBJECT_NAME: {
				objectName = value;
				state = State.OBJECT;
				break;
			}
			case OBJECT_HASH: {
				objectHash = Hex.fromHex(value);
				state = State.OBJECT;
				break;
			}
			case OBJECT_CONTENT_TYPE: {
				// TODO: Share strings?
				objectContentType = value;
				state = State.OBJECT;
				break;
			}
			case OBJECT_LAST_MODIFIED: {
				Date lastModifiedDate;
				try {
					lastModifiedDate = Dates.parse(value);
				} catch (ParseException e) {
					throw new IllegalStateException("Error parsing date value: " + value, e);
				}
				objectLastModified = lastModifiedDate.getTime();
				state = State.OBJECT;
				break;
			}

			}
		}

		@Override
		public void beginArray() {
			switch (state) {
			case ROOT: {
				state = State.CONTAINER;
				break;
			}
			default: {
				throw new IllegalArgumentException();
			}
			}
		}

		@Override
		public void endObject() {
			switch (state) {
			case OBJECT: {
				listener.gotObjectDetails(objectName, objectHash, objectBytes,
						objectContentType, objectLastModified);

				state = State.CONTAINER;
				break;
			}
			default: {
				throw new IllegalArgumentException();
			}
			}
		}

		@Override
		public void gotKey(String localName) {
			switch (state) {

			case OBJECT: {
				if (localName.equals("name")) {
					state = State.OBJECT_NAME;
				} else if (localName.equals("hash")) {
					state = State.OBJECT_HASH;
				} else if (localName.equals("bytes")) {
					state = State.OBJECT_BYTES;
				} else if (localName.equals("content_type")) {
					state = State.OBJECT_CONTENT_TYPE;
				} else if (localName.equals("last_modified")) {
					state = State.OBJECT_LAST_MODIFIED;
				} else {
					throw new IllegalStateException();
				}

				return;
				// break;
			}
			}

			throw new IllegalArgumentException("Unexpected element: "
					+ localName);
		}

		@Override
		public void endDocument() {
			result = listener.endObjects();
		}

	}

	@Override
	protected void gotData(ByteBuffer byteBuffer, boolean isLast) {
		parser.feed(byteBuffer, isLast);
	}

	@Override
	public Object getResult() {
		return result;
	}

}
