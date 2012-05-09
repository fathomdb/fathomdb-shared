package com.fathomdb.dns.server.config;

import org.xbill.DNS.Zone;

import com.fathomdb.config.ConfigObject;

public class DnsZoneConfig extends ConfigObject {

	final Zone zone;

	public DnsZoneConfig(String key, String version, Zone zone) {
		super(key, version);
		this.zone = zone;
	}

}
