package com.fathomdb.proxy.crypto;

import java.security.MessageDigest;

public class Md5Digest extends MessageDigestBase {

	@Override
	protected MessageDigest buildDigest() {
		return buildDigest("MD5");
	}

}