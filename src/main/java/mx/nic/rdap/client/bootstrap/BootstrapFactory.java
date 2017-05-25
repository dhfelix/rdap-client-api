package mx.nic.rdap.client.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import mx.nic.rdap.client.api.Configuration;

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

	public static void init() throws BootstrapException {
		Properties properties = Configuration.getConfiguration();
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
			asnBootstrap = new ASNBootstrap(jsonObject);

		jsonObject = getJsonObject(IPV4_URL);
		if (Objects.nonNull(jsonObject))
			ipv4Bootstrap = new IpBootstrap(jsonObject);

		jsonObject = getJsonObject(IPV6_URL);
		if (Objects.nonNull(jsonObject))
			ipv6Bootstrap = new IpBootstrap(jsonObject);

		jsonObject = getJsonObject(DNS_URL);
		if (Objects.nonNull(jsonObject))
			dnsBootstrap = new DNSBoostrap(jsonObject);
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
		return asnBootstrap;
	}

	public static DNSBoostrap getDnsBootstrap() {
		return dnsBootstrap;
	}

	public static IpBootstrap getIpv4Bootstrap() {
		return ipv4Bootstrap;
	}

	public static IpBootstrap getIpv6Bootstrap() {
		return ipv6Bootstrap;
	}

}
