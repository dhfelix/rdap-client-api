package mx.nic.rdap.client.bootstrap;

import java.util.ArrayList;
import java.util.List;

public class RdapService {

	private List<String> entries;
	private List<String> servicesURL;

	public RdapService() {
		entries = new ArrayList<String>();
		servicesURL = new ArrayList<String>();
	}

	public List<String> getEntries() {
		return entries;
	}

	public List<String> getServicesURL() {
		return servicesURL;
	}

	public void setEntries(List<String> entries) {
		this.entries = entries;
	}

	public void setServicesURL(List<String> servicesURL) {
		this.servicesURL = servicesURL;
	}

	public void addEntry(String entry) {
		this.entries.add(entry);
	}

	public void addServiceURL(String serviceURL) {
		// Always add https urls;
		int index;
		if (serviceURL.startsWith("https")) {
			index = 0;
		} else {
			index = servicesURL.size();
		}
		this.servicesURL.add(index, serviceURL);
	}

	@Override
	public String toString() {
		return "RdapService [entries=" + entries + ", servicesURL=" + servicesURL + "]";
	}

}
