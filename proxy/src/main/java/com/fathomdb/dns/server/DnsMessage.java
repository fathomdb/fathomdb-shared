package com.fathomdb.dns.server;

import java.io.IOException;

import org.xbill.DNS.Message;

public class DnsMessage {
	final Message message;
	private final byte[] messageData;

	public DnsMessage(byte[] messageData) throws IOException {
		this.messageData = messageData;

		this.message = new Message(messageData);
	}

	public Message getMessage() {
		return message;
	}

	public byte[] getOriginalMessageData() {
		return messageData;
	}

}
