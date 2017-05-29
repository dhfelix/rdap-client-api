package mx.nic.rdap.client.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import mx.nic.rdap.client.api.APIConfiguration;

public class BootstrapFactory {

	private static String DNS_URL;
	private static String IPV4_URL;
	private static String IPV6_URL;
	private static String ASN_URL;

	private static final String dnsKey = "bootstrap.dns_url";
	private static final String ipv4Key = "bootstrap.ipv4_url";
	private static final String ipv6Key = "bootstrap.ipv6_url";
	private static final String asnKey = "bootstrap.asn_url";

	private static ASNBootstrap asnBootstrap;
	private static IpBootstrap ipv4Bootstrap;
	private static IpBootstrap ipv6Bootstrap;
	private static DNSBoostrap dnsBootstrap;

	private static ReadWriteLock asnLock = new ReentrantReadWriteLock();
	private static ReadWriteLock ipv4Lock = new ReentrantReadWriteLock();
	private static ReadWriteLock ipv6Lock = new ReentrantReadWriteLock();
	private static ReadWriteLock dnsLock = new ReentrantReadWriteLock();

	public static void init() throws BootstrapException {
		Properties properties = APIConfiguration.getConfiguration();
		DNS_URL = properties.getProperty(dnsKey);
		IPV4_URL = properties.getProperty(ipv4Key);
		IPV6_URL = properties.getProperty(ipv6Key);
		ASN_URL = properties.getProperty(asnKey);

		try {
			updateBootstrap();
		} catch (IOException e) {
			throw new BootstrapException(e);
		}
	}

	public static void updateBootstrap() throws MalformedURLException, IOException, BootstrapException {
		JsonObject jsonObject = getJsonObject(ASN_URL);
		if (Objects.nonNull(jsonObject))
			setAsnBootstrap(new ASNBootstrap(jsonObject));

		jsonObject = getJsonObject(IPV4_URL);
		if (Objects.nonNull(jsonObject))
			setIpv4Bootstrap(new IpBootstrap(jsonObject));

		jsonObject = getJsonObject(IPV6_URL);
		if (Objects.nonNull(jsonObject))
			setIpv6Bootstrap(new IpBootstrap(jsonObject));

		jsonObject = getJsonObject(DNS_URL);
		if (Objects.nonNull(jsonObject))
			setDnsBootstrap(new DNSBoostrap(jsonObject));
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
		}

		return jsonObject;
	}

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

	public static DNSBoostrap getDnsBootstrap() {
		DNSBoostrap result;
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

	public static void setDnsBootstrap(DNSBoostrap dnsBootstrap) {
		try {
			dnsLock.writeLock().lock();
			BootstrapFactory.dnsBootstrap = dnsBootstrap;
		} finally {
			dnsLock.writeLock().unlock();
		}
	}

}
