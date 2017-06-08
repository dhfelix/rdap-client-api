package mx.nic.rdap.client.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public abstract class BootstrapFile {

	public static final String VERSION_KEY = "version";
	public static final String PUBLICATION_KEY = "publication";
	public static final String DESCRIPTION_KEY = "description";
	public static final String SERVICES_KEY = "services";

	private String version;
	private String publicationDate;
	private String Description;
	private List<RdapService> services;

	public BootstrapFile(JsonObject jsonObject) throws BootstrapException {
		if (jsonObject.containsKey(BootstrapFile.DESCRIPTION_KEY)) {
			setDescription(jsonObject.getJsonString(BootstrapFile.DESCRIPTION_KEY).getString());
		}

		if (jsonObject.containsKey(BootstrapFile.VERSION_KEY)) {
			setVersion(jsonObject.getJsonString(BootstrapFile.VERSION_KEY).getString());
		}

		if (jsonObject.containsKey(BootstrapFile.PUBLICATION_KEY)) {
			setPublicationDate(jsonObject.getJsonString(BootstrapFile.PUBLICATION_KEY).getString());
		}

		services = new ArrayList<>();
		if (jsonObject.containsKey(BootstrapFile.SERVICES_KEY)) {
			List<RdapService> parseServices = parseServices(jsonObject.getJsonArray(BootstrapFile.SERVICES_KEY));
			getServices().addAll(parseServices);
		}

	}

	private List<RdapService> parseServices(JsonArray services) throws BootstrapException {
		List<RdapService> rdapServices = new ArrayList<>();

		List<String> invalidUrl = new ArrayList<>();

		for (JsonValue serviceValue : services) {
			JsonArray service = (JsonArray) serviceValue;
			if (service.size() != 2) {
				throw new BootstrapException("Invalid Service:" + service.toString());
			}
			RdapService rdapService = new RdapService();
			for (JsonValue entryValue : service.getJsonArray(0)) {
				JsonString entry = (JsonString) entryValue;
				rdapService.addEntry(entry.getString().toLowerCase());
			}
			for (JsonValue urlValue : service.getJsonArray(1)) {
				JsonString url = (JsonString) urlValue;
				String urlString = url.getString().toLowerCase();
				if (!urlString.endsWith("/")) {
					urlString = urlString + "/";
				}
				if (isValidUrlService(urlString)) {
					rdapService.addServiceURL(urlString);
				} else {
					invalidUrl.add(urlString);
				}
			}
			rdapServices.add(rdapService);
		}

		if (!invalidUrl.isEmpty()) {
			throw new BootstrapException("Invalid url services: " + invalidUrl.toString());
		}

		return rdapServices;
	}

	private boolean isValidUrlService(String urlString) {
		try {
			new URL(urlString);
		} catch (MalformedURLException e) {
			return false;
		}

		return true;
	}

	public String getDescription() {
		return Description;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public String getVersion() {
		return version;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<RdapService> getServices() {
		return services;
	}

	public void setServices(List<RdapService> services) {
		this.services = services;
	}

	public void addService(RdapService service) {
		this.services.add(service);
	}

	@Override
	public String toString() {
		return "BoostrapFile [version=" + version + ", publicationDate=" + publicationDate + ", Description="
				+ Description + ", services=" + services + "]";
	}

}
