package mx.nic.rdap.client.bootstrap;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.json.JsonObject;

import mx.nic.rdap.core.ip.AddressBlock;
import mx.nic.rdap.core.ip.IpAddressFormatException;
import mx.nic.rdap.core.ip.IpUtils;

public class IpBootstrap extends InternetNumbersBootstrap {

	private final static int MIN_CIDR = 0;
	private final static int MAX_IPV4_CIDR = 32;
	private final static int MAX_IPV6_CIDR = 128;

	private static NavigableMap<BigInteger, IpValue> keyForBoostrap;

	public IpBootstrap(JsonObject jsonObject) throws BootstrapException {
		super(jsonObject);

		keyForBoostrap = new TreeMap<>();

		List<RdapService> services = getServices();
		for (RdapService service : services) {
			List<String> entries = service.getEntries();
			List<String> servicesURL = service.getServicesURL();
			for (String entry : entries) {
				String[] split = entry.split("/", 2);
				if (split.length == 1) {
					AddressBlock addressBlock;
					try {
						addressBlock = new AddressBlock(split[0].trim());
					} catch (IpAddressFormatException e) {
						throw new BootstrapException(e);
					}
					BigInteger start = new BigInteger(1, addressBlock.getAddress().getAddress());
					keyForBoostrap.put(start, new IpValue(start, addressBlock.getPrefix(), servicesURL));
				} else {
					AddressBlock addressBlock;
					try {
						addressBlock = new AddressBlock(split[0].trim(), Integer.parseInt(split[1].trim()));
					} catch (NumberFormatException | IpAddressFormatException e) {
						throw new BootstrapException(e);
					}
					BigInteger start = new BigInteger(1, addressBlock.getAddress().getAddress());
					BigInteger end = new BigInteger(1, addressBlock.getLastAddress().getAddress());
					keyForBoostrap.put(start, new IpValue(end, addressBlock.getPrefix(), servicesURL));
				}

			}
		}

	}

	/**
	 * @param serverId
	 *            id of the server to look its URL for the Rdap Service.
	 * @return A list of Rdap servers for the server <code>serverId</code> or
	 *         null if we don't have the information.
	 * @throws IpAddressFormatException
	 */
	public List<String> getRdapUrlByIpRange(String inetAddress) throws IpAddressFormatException {
		String[] split = inetAddress.split("/", 2);
		InetAddress address;
		int prefix;
		if (split.length == 1) {
			address = IpUtils.parseAddress(split[0].trim());
			prefix = getMaxPrefix(address);
		} else {
			address = IpUtils.parseAddress(split[0].trim());
			prefix = Integer.parseInt(split[1].trim());
			isValidPrefix(address, prefix);
		}

		return getRdapUrlByIpRange(address, prefix);
	}

	private List<String> getRdapUrlByIpRange(InetAddress address, int prefix) {
		BigInteger key = new BigInteger(1, address.getAddress());

		Entry<BigInteger, IpValue> floorEntry = keyForBoostrap.floorEntry(key);
		List<String> result = null;
		if (floorEntry != null && prefix >= floorEntry.getValue().getPrefix()) {

			BigInteger end;
			int maxPrefix = getMaxPrefix(address);

			if (prefix != maxPrefix) {
				end = new BigInteger(1, getLastAddress(address, prefix, maxPrefix).getAddress());
			} else {
				end = key;
			}

			if (end.compareTo(floorEntry.getValue().getUpperValue()) <= 0) {
				result = floorEntry.getValue().getRdapUrls();
			}
		}
		return result;
	}

	private void isValidPrefix(InetAddress address, int prefix) throws IpAddressFormatException {
		int maxPrefix = getMaxPrefix(address);
		if (prefix < MIN_CIDR || prefix > maxPrefix) {
			throw new IpAddressFormatException(
					"Invalid prefix '" + prefix + "' for address '" + address.getHostAddress() + "'");
		}

	}

	private int getMaxPrefix(InetAddress address) {
		if (address instanceof Inet4Address) {
			return MAX_IPV4_CIDR;
		}
		if (address instanceof Inet6Address) {
			return MAX_IPV6_CIDR;
		}
		throw new ClassCastException("Only IPv4 and IPv6 addresses are supported.");
	}

	private InetAddress getLastAddress(InetAddress address, int prefix, int maxPrefix) {
		if (prefix == maxPrefix) {
			return address;
		}

		byte[] bytes = address.getAddress();

		// Fill 1's in the prefix's byte.
		bytes[prefix >> 3] |= 0xFF >>> (prefix & 7);
		// Fill 1's in the remaining bytes.
		for (int i = (prefix >> 3) + 1; i < bytes.length; i++) {
			bytes[i] = (byte) 0xFF;
		}

		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Automatically-generated array has an invalid length.");
		}
	}

	class IpValue {
		private BigInteger upperValue;

		private int prefix;

		private List<String> rdapUrls;

		public IpValue(BigInteger upperValue, int prefix, List<String> rdapUrls) {
			super();
			this.upperValue = upperValue;
			this.prefix = prefix;
			this.rdapUrls = rdapUrls;
		}

		public BigInteger getUpperValue() {
			return upperValue;
		}

		public void setUpperValue(BigInteger upperValue) {
			this.upperValue = upperValue;
		}

		public int getPrefix() {
			return prefix;
		}

		public void setPrefix(int prefix) {
			this.prefix = prefix;
		}

		public List<String> getRdapUrls() {
			return rdapUrls;
		}

		public void setRdapUrls(List<String> rdapUrls) {
			this.rdapUrls = rdapUrls;
		}

	}
}
