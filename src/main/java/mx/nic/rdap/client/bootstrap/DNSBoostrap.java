package mx.nic.rdap.client.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

public class DNSBoostrap extends BoostrapFile {

	private static Map<String, List<String>> dnsBootstrap;

	// private static Map<String, String> urlToKeyMap;

	public DNSBoostrap(JsonObject jsonObject) {
		super(jsonObject);

		dnsBootstrap = new HashMap<>();
		// urlToKeyMap = new HashMap<>();

		List<RdapService> services = getServices();
		for (RdapService service : services) {
			List<String> entries = service.getEntries();
			List<String> servicesURL = service.getServicesURL();
			for (String entry : entries) {
				dnsBootstrap.put(entry.toLowerCase(), servicesURL);
			}
		}

	}

	/**
	 * @param serverId
	 *            id of the server to look its URL for the Rdap Service.
	 * @return A list of Rdap servers for the server <code>serverId</code> or
	 *         null if we don't have the information.
	 */
	public List<String> getRdapUrlByServerId(String serverId) {
		return dnsBootstrap.get(serverId.toLowerCase());
	}

}
