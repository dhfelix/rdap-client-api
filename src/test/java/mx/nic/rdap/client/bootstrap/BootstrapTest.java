package mx.nic.rdap.client.bootstrap;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import mx.nic.rdap.client.api.APIConfiguration;
import mx.nic.rdap.client.exception.ConfigurationException;
import mx.nic.rdap.core.ip.IpAddressFormatException;

public class BootstrapTest {

	@BeforeClass
	public static void loadConfiguration() throws ConfigurationException, BootstrapException {
		APIConfiguration.initConfiguration(new Properties());
		BootstrapFactory.init();
	}

	@Test
	public void getASN() {
		ASNBootstrap asn = BootstrapFactory.getAsnBootstrap();
		long initSearch = System.nanoTime();

		long key = 14708;
		List<String> rdapUrlByAsn = asn.getRdapUrlByAsn(key);
		long endSearch = System.nanoTime();

		System.out.println("=============================================");
		System.out.println("key :" + key);
		System.out.println("result: " + rdapUrlByAsn);
		System.out.println("search time" + TimeUnit.NANOSECONDS.toMillis(endSearch - initSearch) + " ms");
		System.out.println("=============================================");

	}

	@Test
	public void getIPv4() throws IpAddressFormatException {
		IpBootstrap ip = BootstrapFactory.getIpv4Bootstrap();
		long initSearch = System.nanoTime();

		String key = "192.168.1.254/32";
		List<String> rdapUrlByIp = ip.getRdapUrlByIpRange(key);
		long endSearch = System.nanoTime();

		System.out.println("=============================================");
		System.out.println("key :" + key);
		System.out.println("result: " + rdapUrlByIp);
		System.out.println("search time" + TimeUnit.NANOSECONDS.toMillis(endSearch - initSearch) + " ms");
		System.out.println("=============================================");
	}

	@Test
	public void getIPv6() throws IpAddressFormatException {
		IpBootstrap ip = BootstrapFactory.getIpv6Bootstrap();
		long initSearch = System.nanoTime();

		String key = "2001:db8::1/92";
		List<String> rdapUrlByIp = ip.getRdapUrlByIpRange(key);
		long endSearch = System.nanoTime();

		System.out.println("=============================================");
		System.out.println("key :" + key);
		System.out.println("result: " + rdapUrlByIp);
		System.out.println("search time" + TimeUnit.NANOSECONDS.toMillis(endSearch - initSearch) + " ms");
		System.out.println("=============================================");
	}

	@Test
	public void getDNS() {
		System.out.println("********* dns search ***********");
		DNSBoostrap dns = BootstrapFactory.getDnsBootstrap();

		List<String> rdapUrlByServerId = dns.getRdapUrlByServerId("br");
		System.out.println(rdapUrlByServerId);
		List<String> rdapUrlByServerId2 = dns.getRdapUrlByServerId("MX");
		System.out.println(rdapUrlByServerId2);
		List<String> rdapUrlByServerId3 = dns.getRdapUrlByServerId("BR");
		System.out.println(rdapUrlByServerId3);

	}

	@Test
	public void getServerId() {
		System.out.println("*************  servers id bootstrap  ***************");
		System.out.println(BootstrapFactory.getServerIdBootstrap().getAllServersId());
	}

}
