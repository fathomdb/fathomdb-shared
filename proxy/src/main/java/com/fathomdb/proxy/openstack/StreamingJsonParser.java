package com.fathomdb.proxy.openstack;

import java.nio.ByteBuffer;
import java.util.Stack;

import com.fathomdb.proxy.openstack.JsonHandler.ValueType;
import com.fathomdb.proxy.openstack.StreamingJsonTokenizer.Token;
import com.fathomdb.proxy.openstack.StreamingJsonTokenizer.TokenType;

public class StreamingJsonParser {
	final StreamingJsonTokenizer tokenizer;
	final Stack<State> stateStack = new Stack<State>();
	final Stack<Token> tokenStack = new Stack<Token>();
	final JsonHandler handler;

	enum State {
		IN_ARRAY, IN_OBJECT, IN_VALUE
	}

	public StreamingJsonParser(StreamingJsonTokenizer tokenizer, JsonHandler handler) {
		super();
		this.tokenizer = tokenizer;
		this.handler = handler;
		stateStack.push(State.IN_VALUE);
	}

	public void feed(ByteBuffer in, boolean isLast) {
		tokenizer.feed(in, isLast);

		parse();

		if (isLast) {
			if (stateStack.size() != 0) {
				throw new IllegalStateException();
			}
			handler.endDocument();
		}
	}

	Token nextToken() {
		if (!tokenStack.isEmpty()) {
			return tokenStack.pop();
		} else {
			Token token = tokenizer.nextToken();
			// System.out.println(token);
			return token;
		}
	}

	void pushback(Token token) {
		tokenStack.push(token);
	}

	void parse() {
		while (true) {
			Token next = nextToken();
			if (next == null) {
				return;
			}

			switch (stateStack.peek()) {

			case IN_ARRAY: {
				switch (next.type) {
				case COMMA: {
					pushState(State.IN_VALUE);
					break;
				}

				case CLOSE_SQUARE: {
					handler.endArray();
					popState();
					break;
				}

				default: {
					throw new IllegalStateException();
				}

				}

				break;
			}

			case IN_OBJECT: {
				switch (next.type) {
				case COMMA: {
					// Relaxed parsing of commas
					break;
				}

				case STRING: {
					Token colon = nextToken();
					if (colon == null) {
						pushback(next);
						return;
					}
					if (colon.type != TokenType.COLON) {
						throw new IllegalStateException();
					}
					handler.gotKey(next.value);
					pushState(State.IN_VALUE);
					break;
				}

				case CLOSE_CURLY: {
					handler.endObject();
					popState();
					break;
				}

				default: {
					throw new IllegalStateException();
				}

				}

				break;
			}

			case IN_VALUE: {
				switch (next.type) {
				case STRING: {
					handler.gotValue(ValueType.String, next.value);
					popState();
					break;
				}

				case NUMBER: {
					handler.gotValue(ValueType.Number, next.value);
					popState();
					break;
				}

				case TRUE: {
					handler.gotValue(ValueType.LiteralTrue, null);
					popState();
					break;
				}

				case FALSE: {
					handler.gotValue(ValueType.LiteralFalse, null);
					popState();
					break;
				}

				case JSON_NULL: {
					handler.gotValue(ValueType.LiteralNull, null);
					popState();
					break;
				}

				case OPEN_CURLY: {
					handler.beginObject();
					popState();
					pushState(State.IN_OBJECT);
					break;
				}

				case OPEN_SQUARE: {
					handler.beginArray();
					popState();
					pushState(State.IN_ARRAY);
					pushState(State.IN_VALUE);
					break;
				}

				default: {
					throw new IllegalStateException();
				}
				}
				break;
			}

			}
		}
	}

	private void popState() {
		stateStack.pop();
	}

	void pushState(State state) {
		stateStack.push(state);
	}
}
