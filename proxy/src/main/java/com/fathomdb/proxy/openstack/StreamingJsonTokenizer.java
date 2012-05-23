package com.fathomdb.proxy.openstack;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingJsonTokenizer {
	static final Logger log = LoggerFactory.getLogger(StreamingJsonTokenizer.class);

	final CharsetDecoder decoder;
	CharBuffer charBuffer;
	int readPos;

	public StreamingJsonTokenizer(Charset charset) {
		super();
		this.decoder = charset.newDecoder();
		this.decoder.onMalformedInput(CodingErrorAction.REPORT);

		this.charBuffer = CharBuffer.allocate(8192);
	}

	public StreamingJsonTokenizer() {
		this(Charset.forName("UTF-8"));
	}

	public enum TokenType {
		COLON, OPEN_CURLY, CLOSE_CURLY, OPEN_SQUARE, CLOSE_SQUARE, COMMA, TRUE, FALSE, JSON_NULL, STRING, NUMBER
	};

	public static class Token {
		final TokenType type;
		final String value;

		public Token(TokenType type, String value) {
			super();
			this.type = type;
			this.value = value;
		}

		@Override
		public String toString() {
			return "Token [type=" + type + ", value=" + value + "]";
		}

	}

	public static final Token TOKEN_LITERAL_COMMA = new Token(TokenType.COMMA, ",");

	public static final Token TOKEN_LITERAL_COLON = new Token(TokenType.COLON, ":");

	public static final Token TOKEN_LITERAL_TRUE = new Token(TokenType.TRUE, "true");
	public static final Token TOKEN_LITERAL_FALSE = new Token(TokenType.FALSE, "false");
	public static final Token TOKEN_LITERAL_NULL = new Token(TokenType.JSON_NULL, "null");

	public static final Token TOKEN_LITERAL_OPEN_SQUARE = new Token(TokenType.OPEN_SQUARE, "[");
	public static final Token TOKEN_LITERAL_CLOSE_SQUARE = new Token(TokenType.CLOSE_SQUARE, "]");
	public static final Token TOKEN_LITERAL_OPEN_CURLY = new Token(TokenType.OPEN_CURLY, "{");
	public static final Token TOKEN_LITERAL_CLOSE_CURLY = new Token(TokenType.CLOSE_CURLY, "}");

	Token nextToken() {
		int pos = readPos;
		int limit = charBuffer.position();

		Token token = null;

		char nextChar;
		while (true) {
			if (pos >= limit) {
				nextChar = 0; // Dummy EOF
			} else {
				nextChar = charBuffer.get(pos);
				switch (nextChar) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					pos++;
					continue;
				}
			}

			break;
		}

		switch (nextChar) {
		case 0:
			// Dummy EOF
			break;

		case ',':
			token = TOKEN_LITERAL_COMMA;
			pos++;
			break;

		case ':':
			token = TOKEN_LITERAL_COLON;
			pos++;
			break;

		case '[':
			token = TOKEN_LITERAL_OPEN_SQUARE;
			pos++;
			break;
		case ']':
			token = TOKEN_LITERAL_CLOSE_SQUARE;
			pos++;
			break;
		case '{':
			token = TOKEN_LITERAL_OPEN_CURLY;
			pos++;
			break;
		case '}':
			token = TOKEN_LITERAL_CLOSE_CURLY;
			pos++;
			break;
		case 't': {
			// Must be Literal true
			if ((limit - pos) >= 4) {
				if ((charBuffer.get(pos + 1) == 'r') && (charBuffer.get(pos + 2) == 'u')
						&& (charBuffer.get(pos + 3) == 'e')) {
					token = TOKEN_LITERAL_TRUE;
					pos += 4;
					break;
				} else {
					throw new IllegalStateException();
				}
			}
			break;
		}

		case 'n': {
			// Must be Literal null
			if ((limit - pos) >= 4) {
				if ((charBuffer.get(pos + 1) == 'u') && (charBuffer.get(pos + 2) == 'l')
						&& (charBuffer.get(pos + 3) == 'l')) {
					token = TOKEN_LITERAL_NULL;
					pos += 4;
					break;
				} else {
					throw new IllegalStateException();
				}
			}
			break;
		}

		case 'f': {
			// Must be Literal false
			if ((limit - pos) >= 5) {
				if ((charBuffer.get(pos + 1) == 'a') && (charBuffer.get(pos + 2) == 'l')
						&& (charBuffer.get(pos + 3) == 's') && (charBuffer.get(pos + 4) == 'e')) {
					token = TOKEN_LITERAL_FALSE;
					pos += 4;
					break;
				} else {
					throw new IllegalStateException();
				}
			}
			break;
		}

		case '\"': {
			int start = pos + 1;
			StringBuilder s = new StringBuilder();
			for (int i = start; i < limit; i++) {
				char c = charBuffer.get(i);

				// TODO: Recognize escape characters
				if (c == '\"') {
					token = new Token(TokenType.STRING, s.toString());
					pos = i + 1;
					break;
				} else {
					s.append(c);
				}
			}
			break;
		}

		case '-':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': {
			int start = pos;
			StringBuilder s = new StringBuilder();
			int end = -1;

			for (int i = start; i < limit && (end == -1); i++) {
				char c = charBuffer.get(i);
				switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '+':
				case '-':
				case 'e':
				case 'E':
				case '.': {
					s.append(c);
					break;
				}

				default:
					end = i;
					break;
				}
			}

			if (end != -1) {
				token = new Token(TokenType.NUMBER, s.toString());
				pos = end;
			}
			break;
		}

		default:
			throw new IllegalStateException("Unexpected character: " + ((int) nextChar));
		}

		readPos = pos;

		if (token == null) {
			if (readPos != 0) {
				// We need to move everything down to make room

				int writePos = charBuffer.position();

				charBuffer.limit(writePos);
				charBuffer.position(readPos);
				charBuffer.compact();

				readPos = 0;
			}
			return null;
		}

		return token;
	}

	ByteBuffer previous;

	public void feed(ByteBuffer in, boolean isLast) {
		ByteBuffer parse;

		if (in == null) {
			parse = previous;
			this.previous = null;
		} else if (previous != null) {
			if (previous.remaining() > in.remaining()) {
				int newSize = previous.capacity() + in.remaining() + 8192;
				ByteBuffer bigger = ByteBuffer.allocate(newSize);
				bigger.put(previous);
				previous = bigger;
			}

			previous.put(in);
			parse = previous;
			this.previous = null;
		} else {
			parse = in;
		}

		while (parse != null) {
			int oldPosition = charBuffer.position();

			CoderResult result = decoder.decode(parse, charBuffer, isLast);

			if (log.isDebugEnabled()) {
				int newPosition = charBuffer.position();

				if (newPosition != oldPosition) {
					CharBuffer duplicate = charBuffer.duplicate();
					duplicate.limit(newPosition);
					duplicate.position(oldPosition);
					String s = duplicate.toString();
					log.debug(s);
				}
			}

			if (result.isUnderflow()) {
				// We've processed the entire input buffer. This is good news.
				if (parse.remaining() != 0) {
					if (parse.position() != 0) {
						parse.compact();
					}
					this.previous = parse;
				} else {
					this.previous = null;
				}
				break;
			} else if (result.isOverflow()) {
				// This can happen if e.g. we have a huge key/value
				int newSize = charBuffer.capacity() + 8192;

				if (readPos != 0) {
					throw new IllegalStateException();
				}

				CharBuffer bigger = CharBuffer.allocate(newSize);
				charBuffer.flip();
				bigger.put(charBuffer);
				charBuffer = bigger;
			} else {
				throw new IllegalStateException();
			}
		}

	}

}
