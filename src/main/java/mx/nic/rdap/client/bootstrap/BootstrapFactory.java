package mx.nic.rdap.client.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import mx.nic.rdap.client.api.BootstrapConfiguration;

public class BootstrapFactory {

	private static final Logger logger = Logger.getLogger(BootstrapFactory.class.getName());

	private static BootstrapConfiguration configuration;

	private static ASNBootstrap asnBootstrap;
	private static IpBootstrap ipv4Bootstrap;
	private static IpBootstrap ipv6Bootstrap;
	private static DNSBootstrap dnsBootstrap;
	private static ServerIdBootstrap serverIdBootstrap;

	private static ReadWriteLock asnLock = new ReentrantReadWriteLock();
	private static ReadWriteLock ipv4Lock = new ReentrantReadWriteLock();
	private static ReadWriteLock ipv6Lock = new ReentrantReadWriteLock();
	private static ReadWriteLock dnsLock = new ReentrantReadWriteLock();
	private static ReadWriteLock serverIdLock = new ReentrantReadWriteLock();

	private static Timer timer;

	public synchronized static void init(BootstrapConfiguration bootstrapConfiguration) throws BootstrapException {
		logger.log(Level.INFO, "Loading Bootstrap from IANA.");
		configuration = bootstrapConfiguration;
		try {
			updateBootstrap();
		} catch (IOException e) {
			throw new BootstrapException(e);
		}

		long period = configuration.getTimerPeriod();
		if (period > 0 && timer == null) {
			timer = new Timer("BootstrapUpdaterThread", true);
			timer.schedule(new BootstrapTask(), period, period);
		}
	}

	public static void initFromJson(JsonObject asn, JsonObject ipv4, JsonObject ipv6, JsonObject dns)
			throws BootstrapException {
		logger.log(Level.INFO, "Loading Bootstrap from JSON files.");
		updateBootstrap(asn, ipv4, ipv6, dns);
	}

	private static void updateBootstrap(JsonObject asn, JsonObject ipv4, JsonObject ipv6, JsonObject dns)
			throws BootstrapException {

		// first create bootstrap objects
		ASNBootstrap asnB = null;
		if (Objects.nonNull(asn)) {
			asnB = new ASNBootstrap(asn);
		}
		IpBootstrap ipv4B = null;
		if (Objects.nonNull(ipv4)) {
			ipv4B = new IpBootstrap(ipv4);
		}
		IpBootstrap ipv6B = null;
		if (Objects.nonNull(ipv6)) {
			ipv6B = new IpBootstrap(ipv6);
		}
		DNSBootstrap dnsB = null;
		if (Objects.nonNull(dns)) {
			dnsB = new DNSBootstrap(dns);
		}
		ServerIdBootstrap serverIdB = new ServerIdBootstrap(dnsB, asnB, ipv4B, ipv6B);

		// then update bootstraps
		setAsnBootstrap(asnB);
		setIpv4Bootstrap(ipv4B);
		setIpv6Bootstrap(ipv6B);
		setDnsBootstrap(dnsB);
		setServerIdBootstrap(serverIdB);
	}

	protected static void updateBootstrap() throws MalformedURLException, IOException, BootstrapException {
		JsonObject asn = getJsonObject(configuration.getAsnUrl());

		JsonObject ipv4 = getJsonObject(configuration.getIpv4Url());

		JsonObject ipv6 = getJsonObject(configuration.getIpv6Url());

		JsonObject dns = getJsonObject(configuration.getDnsUrl());

		updateBootstrap(asn, ipv4, ipv6, dns);

	}

	private static JsonObject getJsonObject(String url) throws MalformedURLException, IOException {
		HttpURLConnection httpConnection;
		int responseCode;
		httpConnection = (HttpURLConnection) new URL(url).openConnection();

		responseCode = httpConnection.getResponseCode();
		JsonObject jsonObject = null;
		if (responseCode == 200) {
			try (InputStream ins = httpConnection.getInputStream(); JsonReader jsonReader = Json.createReader(ins);) {
				jsonObject = jsonReader.readObject();
			}
		} else {
			logger.log(Level.WARNING, "cannot read bootstrap from : '" + url + "', responseCode: " + responseCode);
		}

		return jsonObject;
	}

	// getters and setters

	public static ASNBootstrap getAsnBootstrap() {
		ASNBootstrap result;
		try {
			asnLock.readLock().lock();
			result = asnBootstrap;
		} finally {
			asnLock.readLock().unlock();
		}
		return result;
	}

	public static DNSBootstrap getDnsBootstrap() {
		DNSBootstrap result;
		try {
			dnsLock.readLock().lock();
			result = dnsBootstrap;
		} finally {
			dnsLock.readLock().unlock();
		}
		return result;
	}

	public static IpBootstrap getIpv4Bootstrap() {
		IpBootstrap result;
		try {
			ipv4Lock.readLock().lock();
			result = ipv4Bootstrap;
		} finally {
			ipv4Lock.readLock().unlock();
		}
		return result;
	}

	public static IpBootstrap getIpv6Bootstrap() {
		IpBootstrap result;
		try {
			ipv6Lock.readLock().lock();
			result = ipv6Bootstrap;
		} finally {
			ipv6Lock.readLock().unlock();
		}
		return result;
	}

	public static ServerIdBootstrap getServerIdBootstrap() {
		ServerIdBootstrap result;
		try {
			serverIdLock.readLock().lock();
			result = serverIdBootstrap;
		} finally {
			serverIdLock.readLock().unlock();
		}
		return result;
	}

	public static void setAsnBootstrap(ASNBootstrap asnBootstrap) {
		try {
			asnLock.writeLock().lock();
			BootstrapFactory.asnBootstrap = asnBootstrap;
		} finally {
			asnLock.writeLock().unlock();
		}
	}

	public static void setIpv4Bootstrap(IpBootstrap ipv4Bootstrap) {
		try {
			ipv4Lock.writeLock().lock();
			BootstrapFactory.ipv4Bootstrap = ipv4Bootstrap;
		} finally {
			ipv4Lock.writeLock().unlock();
		}
	}

	public static void setIpv6Bootstrap(IpBootstrap ipv6Bootstrap) {
		try {
			ipv6Lock.writeLock().lock();
			BootstrapFactory.ipv6Bootstrap = ipv6Bootstrap;
		} finally {
			ipv6Lock.writeLock().unlock();
		}
	}

	public static void setDnsBootstrap(DNSBootstrap dnsBootstrap) {
		try {
			dnsLock.writeLock().lock();
			BootstrapFactory.dnsBootstrap = dnsBootstrap;
		} finally {
			dnsLock.writeLock().unlock();
		}
	}

	public static void setServerIdBootstrap(ServerIdBootstrap serverIdBootstrap) {
		try {
			serverIdLock.writeLock().lock();
			BootstrapFactory.serverIdBootstrap = serverIdBootstrap;
		} finally {
			serverIdLock.writeLock().unlock();
		}
	}

}
