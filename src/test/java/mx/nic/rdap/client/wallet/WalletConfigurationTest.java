package mx.nic.rdap.client.wallet;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class WalletConfigurationTest {

	@Test
	public void test() {
		Properties configuration = new Properties();
		try (InputStream in = WalletConfigurationTest.class.getClassLoader()
				.getResourceAsStream("META-INF/default_api_configuration.properties")) {
			configuration.load(in);
			new WalletConfiguration(configuration);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
