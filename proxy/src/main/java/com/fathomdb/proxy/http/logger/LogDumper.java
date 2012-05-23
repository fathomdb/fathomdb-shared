package com.fathomdb.proxy.http.logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class LogDumper {
	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);

		DataInputStream dis;

		InputStream is = null;
		try {
			// TODO: Do we need BufferedOutputStream before / after GZIP
			// output stream?
			is = new FileInputStream(file);
			is = new BufferedInputStream(is);
			is = new GZIPInputStream(is);
			dis = new DataInputStream(is);
			is = null;
		} finally {
			if (is != null) {
				is.close();
			}
		}

		try {
			while (true) {
				byte recordType = dis.readByte();

				switch (recordType) {
				case RequestLogger.RECORD_TYPE_HEADER:
					int version = dis.readInt();
					System.out.println("version: " + version);
					break;

				case RequestLogger.RECORD_TYPE_TIMESTAMP:
					long timestamp = dis.readLong();

					System.out.println("timestamp: " + new Date(timestamp));
					break;

				case RequestLogger.RECORD_TYPE_REQUEST: {
					int addressBytesLength = dis.readByte();

					byte[] addressBytes = new byte[addressBytesLength];
					dis.readFully(addressBytes);
					InetAddress remoteAddress;
					if (addressBytesLength == 4) {
						remoteAddress = InetAddress.getByAddress(addressBytes);
					} else {
						throw new IllegalArgumentException("Unhandled address length: " + addressBytesLength);
					}
					int remotePort = dis.readInt();

					String method = dis.readUTF();
					String uri = dis.readUTF();

					byte protocolVersion = dis.readByte();

					System.out.println("REQUEST:\t" + remoteAddress + ":" + remotePort + " " + method + " " + uri + " "
							+ "HTTP/" + protocolVersion);
					int headerCount = dis.readInt();
					for (int i = 0; i < headerCount; i++) {
						String key = dis.readUTF();
						String value = dis.readUTF();
						System.out.println("\t\t" + key + ": " + value);
					}

					break;
				}

				case RequestLogger.RECORD_TYPE_RESPONSE: {
					int statusCode = dis.readShort();
					String statusReason = dis.readUTF();

					System.out.println("RESPONSE:\t" + statusCode + " " + statusReason);
					int headerCount = dis.readInt();
					for (int i = 0; i < headerCount; i++) {
						String key = dis.readUTF();
						String value = dis.readUTF();
						System.out.println("\t\t" + key + ": " + value);
					}

					long responseLength = dis.readLong();
					System.out.println("\t\tLength:" + responseLength);

					break;
				}

				default:
					throw new IllegalArgumentException("Unknown record type: " + recordType);
				}
			}
		} catch (EOFException e) {
			System.out.println("EOF");
		}
	}
}
