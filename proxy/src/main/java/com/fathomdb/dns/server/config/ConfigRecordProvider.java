package com.fathomdb.dns.server.config;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DNAMERecord;
import org.xbill.DNS.ExtendedFlags;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TSIGRecord;
import org.xbill.DNS.Type;
import org.xbill.DNS.Zone;

import com.fathomdb.dns.server.RecordProvider;

public class ConfigRecordProvider implements RecordProvider {
	static final int FLAG_DNSSECOK = 1;
	static final int FLAG_SIGONLY = 2;

	private final DnsZoneConfigProvider config;

	public ConfigRecordProvider(DnsZoneConfigProvider config) {
		this.config = config;
	}

	/*
	 * Note: a null return value means that the caller doesn't need to do anything. Currently this only happens if this
	 * is an AXFR request over TCP.
	 */
	@Override
	public byte[] generateReply(Message query, byte[] in, int length, Socket s) {
		Header header;
		boolean badversion;
		int maxLength;
		int flags = 0;

		header = query.getHeader();
		if (header.getFlag(Flags.QR)) {
			return null;
		}
		if (header.getRcode() != Rcode.NOERROR) {
			return errorMessage(query, Rcode.FORMERR);
		}
		if (header.getOpcode() != Opcode.QUERY) {
			return errorMessage(query, Rcode.NOTIMP);
		}

		Record queryRecord = query.getQuestion();

		TSIGRecord queryTSIG = query.getTSIG();
		TSIG tsig = null;
		if (queryTSIG != null) {
			return errorMessage(query, Rcode.NOTIMP);
			// tsig = TSIGs.get(queryTSIG.getName());
			// if (tsig == null
			// || tsig.verify(query, in, length, null) != Rcode.NOERROR)
			// return formerrMessage(in);
		}

		OPTRecord queryOPT = query.getOPT();
		if (queryOPT != null && queryOPT.getVersion() > 0) {
			badversion = true;
		}

		if (s != null) {
			maxLength = 65535;
		} else if (queryOPT != null) {
			maxLength = Math.max(queryOPT.getPayloadSize(), 512);
		} else {
			maxLength = 512;
		}

		if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0) {
			flags = FLAG_DNSSECOK;
		}

		Message response = new Message(query.getHeader().getID());
		response.getHeader().setFlag(Flags.QR);
		if (query.getHeader().getFlag(Flags.RD)) {
			response.getHeader().setFlag(Flags.RD);
		}
		response.addRecord(queryRecord, Section.QUESTION);

		Name name = queryRecord.getName();
		int type = queryRecord.getType();
		int dclass = queryRecord.getDClass();

		if (type == Type.AXFR && s != null) {
			// return doAXFR(name, query, tsig, queryTSIG, s);
			return errorMessage(query, Rcode.NOTIMP);
		}

		if (!Type.isRR(type) && type != Type.ANY) {
			return errorMessage(query, Rcode.NOTIMP);
		}

		byte rcode = addAnswer(response, name, type, dclass, 0, flags);
		if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN) {
			return errorMessage(query, rcode);
		}

		addAdditional(response, flags);

		if (queryOPT != null) {
			int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
			OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0, optflags);
			response.addRecord(opt, Section.ADDITIONAL);
		}

		response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
		return response.toWire(maxLength);
	}

	private void addAdditional2(Message response, int section, int flags) {
		Record[] records = response.getSectionArray(section);
		for (int i = 0; i < records.length; i++) {
			Record r = records[i];
			Name glueName = r.getAdditionalName();
			if (glueName != null) {
				addGlue(response, glueName, flags);
			}
		}
	}

	private final void addAdditional(Message response, int flags) {
		addAdditional2(response, Section.ANSWER, flags);
		addAdditional2(response, Section.AUTHORITY, flags);
	}

	void addRRset(Name name, Message response, RRset rrset, int section, int flags) {
		for (int s = 1; s <= section; s++) {
			if (response.findRRset(name, rrset.getType(), s)) {
				return;
			}
		}
		if ((flags & FLAG_SIGONLY) == 0) {
			Iterator it = rrset.rrs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
		if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
			Iterator it = rrset.sigs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
	}

	private final void addSOA(Message response, Zone zone) {
		response.addRecord(zone.getSOA(), Section.AUTHORITY);
	}

	private final void addNS(Message response, Zone zone, int flags) {
		RRset nsRecords = zone.getNS();
		addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
	}

	// private final void addCacheNS(Message response, Cache cache, Name name) {
	// SetResponse sr = cache.lookupRecords(name, Type.NS, Credibility.HINT);
	// if (!sr.isDelegation())
	// return;
	// RRset nsRecords = sr.getNS();
	// Iterator it = nsRecords.rrs();
	// while (it.hasNext()) {
	// Record r = (Record) it.next();
	// response.addRecord(r, Section.AUTHORITY);
	// }
	// }

	public Zone findBestZone(Name name) {
		Zone foundzone = null;
		foundzone = config.getZone(name);
		if (foundzone != null) {
			return foundzone;
		}
		int labels = name.labels();
		for (int i = 1; i < labels; i++) {
			Name tname = new Name(name, i);
			foundzone = config.getZone(tname);
			if (foundzone != null) {
				return foundzone;
			}
		}
		return null;
	}

	// public Cache getCache(int dclass) {
	// Cache c = (Cache) caches.get(new Integer(dclass));
	// if (c == null) {
	// c = new Cache(dclass);
	// caches.put(new Integer(dclass), c);
	// }
	// return c;
	// }

	public RRset findExactMatch(Name name, int type, int dclass, boolean glue) {
		Zone zone = findBestZone(name);
		if (zone != null) {
			return zone.findExactMatch(name, type);
		} else {
			return null;
		}

		// else {
		// RRset[] rrsets;
		// Cache cache = getCache(dclass);
		// if (glue)
		// rrsets = cache.findAnyRecords(name, type);
		// else
		// rrsets = cache.findRecords(name, type);
		// if (rrsets == null)
		// return null;
		// else
		// return rrsets[0]; /* not quite right */
		// }
	}

	private void addGlue(Message response, Name name, int flags) {
		RRset a = findExactMatch(name, Type.A, DClass.IN, true);
		if (a == null) {
			return;
		}
		addRRset(name, response, a, Section.ADDITIONAL, flags);
	}

	byte addAnswer(Message response, Name name, int type, int dclass, int iterations, int flags) {
		SetResponse sr = null;
		byte rcode = Rcode.NOERROR;

		if (iterations > 6) {
			return Rcode.NOERROR;
		}

		if (type == Type.SIG || type == Type.RRSIG) {
			type = Type.ANY;
			flags |= FLAG_SIGONLY;
		}

		Zone zone = findBestZone(name);
		if (zone != null) {
			sr = zone.findRecords(name, type);
		}

		// else {
		// Cache cache = getCache(dclass);
		// sr = cache.lookupRecords(name, type, Credibility.NORMAL);
		// }

		// if (sr.isUnknown()) {
		// addCacheNS(response, getCache(dclass), name);
		// }

		if (sr == null || sr.isNXDOMAIN()) {
			response.getHeader().setRcode(Rcode.NXDOMAIN);
			if (zone != null) {
				addSOA(response, zone);
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			}
			rcode = Rcode.NXDOMAIN;
		} else if (sr.isNXRRSET()) {
			if (zone != null) {
				addSOA(response, zone);
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			}
		} else if (sr.isDelegation()) {
			RRset nsRecords = sr.getNS();
			addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
		} else if (sr.isCNAME()) {
			CNAMERecord cname = sr.getCNAME();
			RRset rrset = new RRset(cname);
			addRRset(name, response, rrset, Section.ANSWER, flags);
			if (zone != null && iterations == 0) {
				response.getHeader().setFlag(Flags.AA);
			}
			rcode = addAnswer(response, cname.getTarget(), type, dclass, iterations + 1, flags);
		} else if (sr.isDNAME()) {
			DNAMERecord dname = sr.getDNAME();
			RRset rrset = new RRset(dname);
			addRRset(name, response, rrset, Section.ANSWER, flags);
			Name newname;
			try {
				newname = name.fromDNAME(dname);
			} catch (NameTooLongException e) {
				return Rcode.YXDOMAIN;
			}
			rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
			addRRset(name, response, rrset, Section.ANSWER, flags);
			if (zone != null && iterations == 0) {
				response.getHeader().setFlag(Flags.AA);
			}
			rcode = addAnswer(response, newname, type, dclass, iterations + 1, flags);
		} else if (sr.isSuccessful()) {
			RRset[] rrsets = sr.answers();
			for (int i = 0; i < rrsets.length; i++) {
				addRRset(name, response, rrsets[i], Section.ANSWER, flags);
			}
			if (zone != null) {
				addNS(response, zone, flags);
				if (iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
			}
			// else
			// addCacheNS(response, getCache(dclass), name);
		}
		return rcode;
	}

	byte[] buildErrorMessage(Header header, int rcode, Record question) {
		Message response = new Message();
		response.setHeader(header);
		for (int i = 0; i < 4; i++) {
			response.removeAllRecords(i);
		}
		if (rcode == Rcode.SERVFAIL) {
			response.addRecord(question, Section.QUESTION);
		}
		header.setRcode(rcode);
		return response.toWire();
	}

	public byte[] formerrMessage(byte[] in) {
		Header header;
		try {
			header = new Header(in);
		} catch (IOException e) {
			return null;
		}
		return buildErrorMessage(header, Rcode.FORMERR, null);
	}

	public byte[] errorMessage(Message query, int rcode) {
		return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
	}

}