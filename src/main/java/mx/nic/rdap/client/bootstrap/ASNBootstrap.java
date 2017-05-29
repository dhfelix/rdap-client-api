package mx.nic.rdap.client.bootstrap;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.json.JsonObject;

public class ASNBootstrap extends InternetNumbersBootstrap {

	private static NavigableMap<Long, ASNValue> keyForBoostrap;

	public ASNBootstrap(JsonObject jsonObject) throws BootstrapException {
		super(jsonObject);

		keyForBoostrap = new TreeMap<>();

		List<RdapService> services = getServices();
		for (RdapService service : services) {
			List<String> entries = service.getEntries();
			List<String> servicesURL = service.getServicesURL();
			for (String entry : entries) {
				String[] split = entry.split("-", 2);
				if (split.length == 1) {
					Long start = Long.parseLong(split[0]);
					keyForBoostrap.put(start, new ASNValue(start, servicesURL));
				} else {
					Long start = Long.parseLong(split[0]);
					Long end = Long.parseLong(split[1]);
					keyForBoostrap.put(start, new ASNValue(end, servicesURL));
				}

			}
		}

	}

	/**
	 * @param serverId
	 *            id of the server to look its URL for the Rdap Service.
	 * @return A list of Rdap servers for the server <code>serverId</code> or
	 *         null if we don't have the information.
	 */
	public List<String> getRdapUrlByAsn(long asn) {
		Entry<Long, ASNValue> floorEntry = keyForBoostrap.floorEntry(asn);
		List<String> result = null;
		if (floorEntry != null && asn <= floorEntry.getValue().getUpperValue()) {
			result = floorEntry.getValue().getRdapUrls();
		}
		return result;
	}

	class ASNValue {
		private long upperValue;

		private List<String> rdapUrls;

		public ASNValue(long upperValue, List<String> rdapUrls) {
			super();
			this.upperValue = upperValue;
			this.rdapUrls = rdapUrls;
		}

		public long getUpperValue() {
			return upperValue;
		}

		public void setUpperValue(long upperValue) {
			this.upperValue = upperValue;
		}

		public List<String> getRdapUrls() {
			return rdapUrls;
		}

		public void setRdapUrls(List<String> rdapUrls) {
			this.rdapUrls = rdapUrls;
		}

	}
}
