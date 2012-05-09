package com.fathomdb.dns.server;

import java.net.Socket;

import org.xbill.DNS.Message;

public interface RecordProvider {
	byte[] generateReply(Message message, byte[] data, int length, Socket s);
}
