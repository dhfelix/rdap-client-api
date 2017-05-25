package mx.nic.rdap.client.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import mx.nic.rdap.client.exception.ConfigurationException;

public class Configuration {

	private static final String PROPERTIES_FILE = "META-INF/default_api_configuration.properties";

	public static Properties configuration;

	public static void initConfiguration(Properties userProperties) throws ConfigurationException {
		Properties p = new Properties();
		try (InputStream inStream = Configuration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
			p.load(inStream);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

		p.putAll(userProperties);
		configuration = p;
	}

	public static Properties getConfiguration() {
		return configuration;
	}

}
