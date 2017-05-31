package mx.nic.rdap.client.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mx.nic.rdap.core.db.DomainLabel;
import mx.nic.rdap.core.db.DomainLabelException;

public class ServerIdBootstrap {

	private Map<String, List<String>> dnsBootrapToServersId;

	private Map<String, List<String>> urlToServerId;

	private List<String> serversId;

	public ServerIdBootstrap(DNSBoostrap dnsBoostrap, ASNBootstrap asnBootstrap, IpBootstrap ipv4Bootstrap,
			IpBootstrap ipv6Bootstrap) throws BootstrapException {
		dnsBootrapToServersId = new HashMap<>();
		urlToServerId = new HashMap<>();
		Set<String> serversIdSet = new HashSet<>();

		if (dnsBoostrap != null)
			putDNSBoostrapURL(dnsBoostrap, dnsBootrapToServersId, serversIdSet);
		try {
			if (asnBootstrap != null)
				putInternetNumberBootstrapURL(asnBootstrap, urlToServerId, serversIdSet);
			if (ipv4Bootstrap != null)
				putInternetNumberBootstrapURL(ipv4Bootstrap, urlToServerId, serversIdSet);
			if (ipv6Bootstrap != null)
				putInternetNumberBootstrapURL(ipv6Bootstrap, urlToServerId, serversIdSet);
		} catch (MalformedURLException e) {
			throw new BootstrapException(e);
		}

		serversId = new ArrayList<>(serversIdSet);
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

	private void putInternetNumberBootstrapURL(InternetNumbersBootstrap bootstrap, Map<String, List<String>> map,
			Set<String> serverIdSet) throws MalformedURLException {
		List<RdapService> services = bootstrap.getServices();
		for (RdapService service : services) {
			for (String url : service.getServicesURL()) {
				// if serverId for url exist, continue with next url.
				if (map.containsKey(url)) {
					continue;
				}

				// gets server id from URL
				String serverIdFromURL = getServerIdFromURL(new URL(url));

				List<String> value = map.get(serverIdFromURL);
				if (value == null) {
					value = new ArrayList<>();
					value.add(serverIdFromURL);
					serverIdSet.add(serverIdFromURL);
					map.put(serverIdFromURL, value);
				}
				map.put(url, value);
			}
		}
	}

	private String getServerIdFromURL(URL url) {
		String[] split = url.getHost().split("\\.");
		return split[split.length - 2].toLowerCase() + "." + split[split.length - 1];
	}

	private void putDNSBoostrapURL(DNSBoostrap dns, Map<String, List<String>> map, Set<String> serverIdSet) {
		List<RdapService> services = dns.getServices();
		for (RdapService service : services) {
			List<String> entries = service.getEntries();
			serverIdSet.addAll(entries);
			for (String url : service.getServicesURL()) {
				List<String> values = map.get(url);
				if (values != null) {
					List<String> newList = new ArrayList<>(values);
					newList.addAll(entries);
					entries = newList;
				}
				map.put(url, entries);
			}
		}
	}

	public List<String> getServerIdByRdapURL(String url) {
		List<String> result;
		result = dnsBootrapToServersId.get(url);
		if (result != null) {
			return result;
		}

		return urlToServerId.get(url);

	}

	public List<String> getAllServersId() {
		return serversId;
	}

}
