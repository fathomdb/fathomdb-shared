package com.fathomdb.dns.server.config;

import org.xbill.DNS.Zone;

import com.fathomdb.config.ConfigObject;

public class DnsZoneConfig extends ConfigObject {
	public static final DnsZoneConfig NOT_PRESENT = new DnsZoneConfig(null, null);

	final Zone zone;

	public DnsZoneConfig(String version, Zone zone) {
		super(version);
		this.zone = zone;
	}

	@Override
	public boolean isPresent() {
		return zone != null;
	}

}
