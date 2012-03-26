package com.fathomdb.proxy.openstack.fs;

import java.util.Map;

import com.google.common.collect.Maps;

public class OpenstackDirectory extends OpenstackItem {
	final Map<String, OpenstackItem> children = Maps.newHashMap();

	@Override
	public boolean isFile() {
		return false;
	}
}
