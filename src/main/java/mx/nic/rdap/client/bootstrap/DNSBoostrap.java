package mx.nic.rdap.client.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonObject;

import mx.nic.rdap.core.db.DomainLabel;
import mx.nic.rdap.core.db.DomainLabelException;

public class DNSBoostrap extends BoostrapFile {

	private Map<String, List<String>> dnsBootstrap;

	private List<String> serversId;

	public DNSBoostrap(JsonObject jsonObject) throws BootstrapException {
		super(jsonObject);

		dnsBootstrap = new HashMap<>();
		Set<String> set = new HashSet<>();
		serversId = new ArrayList<String>();

		List<RdapService> services = getServices();
		for (RdapService service : services) {
			List<String> entries = service.getEntries();
			List<String> servicesURL = service.getServicesURL();
			for (String entry : entries) {
				set.add(entry.toLowerCase());
				dnsBootstrap.put(entry.toLowerCase(), servicesURL);
			}
		}

		serversId.addAll(set);
		validateServersId(serversId);
	}

	private void validateServersId(List<String> serversId) throws BootstrapException {
		List<String> invalidServersId = new ArrayList<>();
		for (String s : serversId) {
			try {
				DomainLabel domainLabel = new DomainLabel(s);
				if (!domainLabel.getLabel().equals(domainLabel.getALabel())) {
					invalidServersId.add(s);
				}
			} catch (DomainLabelException e) {
				invalidServersId.add(s);
			}
		}
		if (!invalidServersId.isEmpty()) {
			throw new BootstrapException("Invalid serversId : " + invalidServersId);
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

	@Override
	public List<String> getServersId() {
		return serversId;
	}

}
