package com.fathomdb.dns.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Master;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Zone;

import com.google.common.collect.Lists;

public class DnsServer {
	static final Logger log = LoggerFactory.getLogger(DnsServer.class);

	private NioDatagramChannelFactory serverChannelFactory;

	private ConnectionlessBootstrap bootstrap;

	private DefaultChannelGroup group;

	final ServerConfiguration config;

	public DnsServer(ServerConfiguration config) {
		this.config = config;
	}

	// @Override
	public void initialize() {
		// LOG.debug(Markers.LIFECYCLE, "initialize server");
		RecordProvider recordProvider;
		try {
			recordProvider = buildRecordProvider();
		} catch (IOException e) {
			throw new IllegalStateException("Error loading zones", e);
		}

		ChannelPipelineFactory pipelineFactory = new DnsServerPipelineFactory(
				recordProvider);

		ExecutorService executor = Executors.newFixedThreadPool(this.config
				.getThreadPoolSize());
		// TODO TCP ??
		this.serverChannelFactory = new NioDatagramChannelFactory(executor);

		this.bootstrap = new ConnectionlessBootstrap(this.serverChannelFactory);
		this.bootstrap.setPipelineFactory(pipelineFactory);

		this.group = new DefaultChannelGroup();
	}

	private RecordProvider buildRecordProvider() throws IOException {
		RecordProvider recordProvider = new RecordProvider();

		File baseDir = new File("zones");
		for (File zoneFile : baseDir.listFiles()) {
			Name zoneName = new Name(zoneFile.getName() + ".");

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(zoneFile);
				Master master = new Master(fis, zoneName);
				List<Record> records = Lists.newArrayList();

				Record record;
				while ((record = master.nextRecord()) != null)
					records.add(record);

				Record[] recordArray = (Record[]) records
						.toArray(new Record[records.size()]);
				Zone zone = new Zone(zoneName, recordArray);
				recordProvider.znames.put(zoneName, zone);
			} finally {
				fis.close();
			}
		}
		return recordProvider;
	}

	public void process() {
		for (SocketAddress sa : this.config.getBindingHosts()) {
			// LOG.info(Markers.BOUNDARY, "binding {}", sa);
			this.group.add(this.bootstrap.bind(sa));
		}
	}

	// @Override
	public void dispose() {
		try {
			this.group.close().awaitUninterruptibly();
		} finally {
			dispose(this.serverChannelFactory);
		}
	}

	protected void dispose(ExternalResourceReleasable releasable) {
		try {
			releasable.releaseExternalResources();
		} catch (Exception e) {
			log.error("Error releasing resource", e);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		// We allow the port to be configured in case we want to use iptables to
		// avoid root privileges
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 53;
		}

		ServerConfiguration conf = new ServerConfiguration();
		conf.addBindingHost(new InetSocketAddress(port));
		final DnsServer server = new DnsServer(conf);
		server.initialize();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.dispose();
			}
		});

		server.process();

	}
}
