package com.fathomdb.proxy.http.logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import com.fathomdb.proxy.http.server.GenericRequest;

public class RequestLogger {
	static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

	final BlockingQueue<LogRequest> queue = new LinkedBlockingDeque<LogRequest>();

	public static class LogRequest {
		private final GenericRequest request;
		private final HttpResponse response;
		private final long responseLength;
		private final SocketAddress remoteAddress;

		public LogRequest(SocketAddress remoteAddress, GenericRequest request, HttpResponse response,
				long responseLength) {
			this.remoteAddress = remoteAddress;
			this.request = request;
			this.response = response;
			this.responseLength = responseLength;
		}
	}

	public static final byte RECORD_TYPE_HEADER = 1;
	public static final byte RECORD_TYPE_TIMESTAMP = 8;
	public static final byte RECORD_TYPE_REQUEST = 16;
	public static final byte RECORD_TYPE_RESPONSE = 17;

	public class LoggingThread implements Runnable {
		final File logFile;
		final DataOutputStream output;

		public LoggingThread(File logFile) throws IOException {
			this.logFile = logFile;
			OutputStream os = null;
			try {
				// TODO: Do we need BufferedOutputStream before / after GZIP
				// output stream?
				os = new FileOutputStream(logFile);
				os = new GZIPOutputStream(os, true);
				os = new BufferedOutputStream(os);
				this.output = new DataOutputStream(os);
				os = null;
			} finally {
				if (os != null)
					os.close();
			}
			
			writeHeader();
		}

		static final int FILE_FORMAT_VERSION = 1;
		
		private void writeHeader() throws IOException {
			output.writeByte(RECORD_TYPE_HEADER);
			output.writeInt(FILE_FORMAT_VERSION);
		}

		final CountDownLatch keepRunning = new CountDownLatch(1);

		@Override
		public void run() {
			while (keepRunning.getCount() != 0) {
				try {
					poll();
				} catch (IOException t) {
					// TODO: Rotate file??
					log.warn("Ignoring exception writing log messages", t);
				} catch (Throwable t) {
					// TODO: Rotate file??
					log.warn("Ignoring exception writing log messages", t);
				}
			}

			try {
				output.close();
			} catch (IOException e) {
				log.warn("Ignoring error closing output file");
			}
		}

		public void poll() throws IOException {
			int wroteCount = 0;
			long lastTimestamp = 0;

			while (wroteCount < 1000) {
				LogRequest next = null;

				try {
					if (wroteCount == 0) {
						next = queue.take();
					} else {
						next = queue.poll(5, TimeUnit.SECONDS);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.warn("Interrupted while dequeueing log messages", e);
				}

				if (next == null)
					break;

				long timestamp = System.currentTimeMillis();
				if (lastTimestamp == 0 || (timestamp - lastTimestamp) > 100) {
					output.writeByte(RECORD_TYPE_TIMESTAMP);
					output.writeLong(timestamp);
				}

				lastTimestamp = timestamp;

				{
					output.writeByte(RECORD_TYPE_REQUEST);
					GenericRequest request = next.request;

					InetSocketAddress remoteAddress = (InetSocketAddress) next.remoteAddress;
					byte[] addressBytes = remoteAddress.getAddress().getAddress();
					output.writeByte(addressBytes.length);
					output.write(addressBytes);
					output.writeInt(remoteAddress.getPort());
					
					output.writeUTF(request.getMethod().getName());
					output.writeUTF(request.getUri());

					HttpVersion protocolVersion = request.getProtocolVersion();
					if (protocolVersion == HttpVersion.HTTP_1_0) {
						output.writeByte(10);
					} else if (protocolVersion == HttpVersion.HTTP_1_1) {
						output.writeByte(11);
					} else {
						output.writeByte(0);
					}

					List<Entry<String, String>> headers = request.getHeaders();
					output.writeInt(headers.size());
					for (Map.Entry<String, String> header : headers) {
						output.writeUTF(header.getKey());
						output.writeUTF(header.getValue());
					}

					// TODO: Any way to get request content length??
				}

				{
					output.writeByte(RECORD_TYPE_RESPONSE);
					HttpResponse response = next.response;

					HttpResponseStatus status = response.getStatus();
					output.writeShort(status.getCode());
					output.writeUTF(status.getReasonPhrase());

					List<Entry<String, String>> headers = response.getHeaders();
					output.writeInt(headers.size());
					for (Map.Entry<String, String> header : headers) {
						output.writeUTF(header.getKey());
						output.writeUTF(header.getValue());
					}

					output.writeLong(next.responseLength);
				}
				
				wroteCount++;
			}

			if (wroteCount != 0) {
				output.flush();
			}
		}
	}

	final Thread loggingThread;

	public RequestLogger(File logFile) throws IOException {
		LoggingThread logger = new LoggingThread(logFile);
		loggingThread = new Thread(logger);
		loggingThread.start();
	}

	public void logResponse(SocketAddress remoteAddress, GenericRequest request, HttpResponse response,
			long responseLength) {
		LogRequest logRequest = new LogRequest(remoteAddress, request, response,
				responseLength);
		try {
			queue.put(logRequest);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Interrupted while logging", e);
		}
	}
}
