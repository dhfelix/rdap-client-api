package mx.nic.rdap.client.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class BoostrapFile {

	public static final String VERSION_KEY = "version";
	public static final String PUBLICATION_KEY = "publication";
	public static final String DESCRIPTION_KEY = "description";
	public static final String SERVICES_KEY = "services";

	private String version;
	private String publicationDate;
	private String Description;
	private List<RdapService> services;

	public BoostrapFile(JsonObject jsonObject) {
		if (jsonObject.containsKey(BoostrapFile.DESCRIPTION_KEY)) {
			setDescription(jsonObject.getJsonString(BoostrapFile.DESCRIPTION_KEY).getString());
		}

		if (jsonObject.containsKey(BoostrapFile.VERSION_KEY)) {
			setVersion(jsonObject.getJsonString(BoostrapFile.VERSION_KEY).getString());
		}

		if (jsonObject.containsKey(BoostrapFile.PUBLICATION_KEY)) {
			setPublicationDate(jsonObject.getJsonString(BoostrapFile.PUBLICATION_KEY).getString());
		}

		services = new ArrayList<>();
		if (jsonObject.containsKey(BoostrapFile.SERVICES_KEY)) {
			List<RdapService> parseServices = parseServices(jsonObject.getJsonArray(BoostrapFile.SERVICES_KEY));
			getServices().addAll(parseServices);
		}

	}

	private List<RdapService> parseServices(JsonArray services) {
		List<RdapService> rdapServices = new ArrayList<>();

		for (JsonValue serviceValue : services) {
			JsonArray service = (JsonArray) serviceValue;
			if (service.size() != 2) {
				// TODO throw an exception.
				System.err.println("Invalid service : " + service.toString());
				continue;
			}
			RdapService rdapService = new RdapService();
			for (JsonValue entryValue : service.getJsonArray(0)) {
				JsonString entry = (JsonString) entryValue;
				rdapService.addEntry(entry.getString());
			}
			for (JsonValue urlValue : service.getJsonArray(1)) {
				JsonString url = (JsonString) urlValue;
				rdapService.addServiceURL(url.getString());
			}
			rdapServices.add(rdapService);
		}

		return rdapServices;
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
