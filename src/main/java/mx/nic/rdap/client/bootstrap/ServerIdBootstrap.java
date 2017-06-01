package mx.nic.rdap.client.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mx.nic.rdap.core.db.DomainLabel;
import mx.nic.rdap.core.db.DomainLabelException;

public class ServerIdBootstrap {

	private Map<String, List<String>> rdapUrlToId;

	private Map<String, List<String>> idToRdapUrl;

	private List<String> serversId;

	protected ServerIdBootstrap(DNSBoostrap dnsBoostrap, ASNBootstrap asnBootstrap, IpBootstrap ipv4Bootstrap,
			IpBootstrap ipv6Bootstrap) throws BootstrapException {

		idToRdapUrl = new HashMap<>();
		try {
			if (asnBootstrap != null)
				bootstrapToId(asnBootstrap, idToRdapUrl);
			if (ipv4Bootstrap != null)
				bootstrapToId(ipv4Bootstrap, idToRdapUrl);
			if (ipv6Bootstrap != null)
				bootstrapToId(ipv6Bootstrap, idToRdapUrl);
		} catch (MalformedURLException e) {
			throw new BootstrapException(e);
		}

		rdapUrlToId = new HashMap<>();
		for (Entry<String, List<String>> entry : idToRdapUrl.entrySet()) {
			List<String> id = new ArrayList<>();
			id.add(entry.getKey());
			for (String url : entry.getValue()) {
				rdapUrlToId.put(url, id);
			}
		}

		if (dnsBoostrap != null) {
			bootstrapToId(dnsBoostrap, idToRdapUrl);
			putDNSBoostrapURL(dnsBoostrap, rdapUrlToId);
		}

		serversId = new ArrayList<>(idToRdapUrl.keySet());
		validateServersId(serversId);
		serversId = Collections.unmodifiableList(serversId);

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

	private void bootstrapToId(InternetNumbersBootstrap bootstrap, Map<String, List<String>> map)
			throws MalformedURLException {
		for (RdapService service : bootstrap.getServices()) {
			for (String url : service.getServicesURL()) {
				String idFromURL = getServerIdFromURL(new URL(url)).toLowerCase();

				List<String> list = map.get(idFromURL);
				if (list == null) {
					list = new ArrayList<>();
					map.put(idFromURL, list);
				}

				if (list.contains(url)) {
					continue;
				}

				int index;
				if (url.startsWith("https")) {
					index = 0;
				} else {
					index = list.size();
				}
				list.add(index, url);
			}
		}
	}

	private void bootstrapToId(DNSBoostrap bootstrap, Map<String, List<String>> map) {
		for (RdapService service : bootstrap.getServices()) {
			for (String entry : service.getEntries()) {
				List<String> list = map.get(entry);
				if (list == null) {
					map.put(entry, service.getServicesURL());
					continue;
				}

				for (String url : service.getServicesURL()) {
					if (list.contains(url)) {
						continue;
					}
					int index;
					if (url.startsWith("https")) {
						index = 0;
					} else {
						index = list.size();
					}
					list.add(index, url);
				}
			}
		}
	}

	private String getServerIdFromURL(URL url) {
		String[] split = url.getHost().split("\\.");
		return split[split.length - 2] + "." + split[split.length - 1];
	}

	private void putDNSBoostrapURL(DNSBoostrap dns, Map<String, List<String>> urlToId) {
		List<RdapService> services = dns.getServices();
		for (RdapService service : services) {
			for (String url : service.getServicesURL()) {
				List<String> values = urlToId.get(url);
				List<String> entries = service.getEntries();
				if (values == null) {
					urlToId.put(url, entries);
					continue;
				}

				boolean listHaveSameIds = false;
				if (values.size() == entries.size()) {
					for (String ids : values) {
						if (!entries.contains(ids)) {
							break;
						}
					}
					listHaveSameIds = true;
				}

				if (!listHaveSameIds) {
					List<String> newList = new ArrayList<>(values);
					for (String id : entries) {
						if (!newList.contains(id)) {
							newList.add(id);
						}
					}
					urlToId.put(url, newList);
				}

			}
		}

	}

	/**
	 * @param url
	 *            A {@link String} url that points to a rdap server.
	 * @return a {@link List} of Ids by <code>url</code>, or null if
	 *         <code>url</code> is not mapped to id.
	 */
	public List<String> getServerIdByRdapURL(String url) {
		return rdapUrlToId.get(url);

	}

	/**
	 * @return All server ids, loaded from the bootstrap files.
	 */
	public List<String> getAllServersId() {
		return serversId;
	}

	/**
	 * @param serverId
	 *            id or zone of an rdap server.
	 * @return A list of urls that point to rdap servers that handle the id
	 *         <code>serverId</code>, Or null if the <code>serverId</code> is
	 *         not handled by a known url.
	 */
	public List<String> getRdapUrlById(String serverId) {
		return idToRdapUrl.get(serverId);
	}

}
