package com.fathomdb.dns.server.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xbill.DNS.Master;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Zone;

import com.fathomdb.config.FilesystemConfigProvider;
import com.google.common.collect.Lists;

public class DnsZoneConfigProvider extends
		FilesystemConfigProvider<DnsZoneConfig> {

	public DnsZoneConfigProvider(File baseDir) {
		super(baseDir);
	}

	@Override
	protected DnsZoneConfig loadConfig(String key, String version,
			InputStream is) throws IOException {
		Name zoneName = new Name(key + ".");

		Master master = new Master(is, zoneName);
		List<Record> records = Lists.newArrayList();

		Record record;
		while ((record = master.nextRecord()) != null)
			records.add(record);

		Record[] recordArray = (Record[]) records.toArray(new Record[records
				.size()]);
		Zone zone = new Zone(zoneName, recordArray);

		return new DnsZoneConfig(version, zone);
	}

	public Zone getZone(Name name) {
		String key = name.toString();
		if (key.endsWith(".")) {
			key = key.substring(0, key.length() - 1);
		}
		if (key.isEmpty())
			return null;

		DnsZoneConfig config = getConfig(key);
		if (config == null)
			return null;
		return config.zone;
	}

	@Override
	protected DnsZoneConfig buildNullResult(String key) {
		return DnsZoneConfig.NOT_PRESENT;
	}

}
