package mx.nic.rdap.client.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public abstract class InternetNumbersBootstrap extends BoostrapFile {

	private List<String> serversId;

	public InternetNumbersBootstrap(JsonObject jsonObject) throws BootstrapException {
		super(jsonObject);
		if (jsonObject.containsKey(BoostrapFile.SERVICES_KEY)) {
			serversId = getServersIdFromURL(jsonObject.getJsonArray(BoostrapFile.SERVICES_KEY));
		}

	}

	private List<String> getServersIdFromURL(JsonArray services) throws BootstrapException {
		List<String> result = new ArrayList<>();
		Set<String> set = new HashSet<>();

		for (JsonValue serviceValue : services) {
			JsonArray service = (JsonArray) serviceValue;
			for (JsonValue urlValue : service.getJsonArray(1)) {
				JsonString url = (JsonString) urlValue;
				String serverId;
				try {
					serverId = getServerIdFromURL(new URL(url.getString()));
				} catch (MalformedURLException e) {
					throw new BootstrapException(e);
				}
				set.add(serverId);
			}
		}

		result.addAll(set);
		return result;
	}

	private String getServerIdFromURL(URL url) {
		String[] split = url.getHost().split("\\.");
		return split[split.length - 2].toLowerCase();
	}

	@Override
	public List<String> getServersId() {
		return serversId;
	}

}
