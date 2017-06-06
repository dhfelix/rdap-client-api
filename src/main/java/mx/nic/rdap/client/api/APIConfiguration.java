package mx.nic.rdap.client.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import mx.nic.rdap.client.exception.ConfigurationException;

/**
 * Class that stores the configuration that other classes in the API may need.
 * The idea is to load all the properties here, and then other classes in the
 * API, obtain and validate the configuration that they need.
 */
public class APIConfiguration {

	private static final String PROPERTIES_FILE = "META-INF/default_api_configuration.properties";

	private static Properties apiConfiguration;

	private static BootstrapConfiguration configuration;

	/**
	 * Loads the default configuration, and then overwrites with the user's
	 * configuration.
	 * 
	 * @param userProperties
	 *            The user configuration
	 * @throws ConfigurationException
	 *             Problems loading the default configuration.
	 */
	public static void initConfiguration(Properties userProperties) throws ConfigurationException {
		Properties p = new Properties();
		try (InputStream inStream = APIConfiguration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
			p.load(inStream);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

		p.putAll(userProperties);
		apiConfiguration = p;

		configuration = new BootstrapConfiguration(apiConfiguration);
	}

	/**
	 * @return All the configuration of the API.
	 */
	public static Properties getConfiguration() {
		return apiConfiguration;
	}

	/**
	 * @return Configuration for the bootstrap
	 * @throws ConfigurationException
	 *             When this API Configuration has not been initialized.
	 */
	public static BootstrapConfiguration getBootstrapConfiguration() throws ConfigurationException {
		if (apiConfiguration == null || configuration == null) {
			throw new ConfigurationException("The API has not been initialized");
		}

		return configuration;
	}

}
