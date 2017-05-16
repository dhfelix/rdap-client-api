package mx.nic.rdap.client.wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import mx.nic.rdap.client.exception.ConfigurationException;

public class WalletConfiguration {

	private static final String USER_HASH_SALT_SIZE = "user.hash_salt_size";
	private static final String USER_HASH_ITERATIONS = "user.hash_iterations";
	private static final String USER_HASH_ALGORITHM = "user.hash_algorithm";
	private static final String USER_KEY_ALGORITHM = "user.key_algorithm";
	private static final String USER_KEY_SIZE = "user.key_size";
	private static final String USER_PBE_ALGORITHM = "user.pbe_algorithm";

	private static final String WALLET_KEY_ALGORITHM = "wallet.key_algorithm";
	private static final String WALLET_KEY_SIZE = "wallet.key_size";

	private int userHashSaltSize;
	private int userHashIterations;
	private String userHashAlgorithm;
	private String userKeyAlgorithm;
	private int userKeySize;
	private String userPBEAlgorithm;
	private String walletKeyAlgorithm;
	private int walletKeySize;

	public WalletConfiguration(Properties properties) throws ConfigurationException {
		List<String> invalidProperties = new ArrayList<>();

		String key = USER_HASH_SALT_SIZE;
		try {
			userHashSaltSize = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		key = USER_HASH_ITERATIONS;
		try {
			userHashIterations = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		key = USER_HASH_ALGORITHM;
		try {
			userHashAlgorithm = getString(properties.getProperty(key));
		} catch (NullPointerException e) {
			invalidProperties.add(key);
		}

		key = USER_KEY_ALGORITHM;
		try {
			userKeyAlgorithm = getString(properties.getProperty(key));
		} catch (NullPointerException e) {
			invalidProperties.add(key);
		}

		key = USER_KEY_SIZE;
		try {
			userKeySize = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		key = USER_PBE_ALGORITHM;
		try {
			userPBEAlgorithm = getString(properties.getProperty(key));
		} catch (NullPointerException e) {
			invalidProperties.add(key);
		}

		key = WALLET_KEY_ALGORITHM;
		try {
			walletKeyAlgorithm = getString(properties.getProperty(key));
		} catch (NullPointerException e) {
			invalidProperties.add(key);
		}

		key = WALLET_KEY_SIZE;
		try {
			walletKeySize = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		if (!invalidProperties.isEmpty()) {
			throw new ConfigurationException("Invalid properties : " + invalidProperties.toString());
		}
	}

	private int getInt(String intValue) throws NumberFormatException {
		return Integer.parseInt(intValue);
	}

	private String getString(String value) {
		Objects.requireNonNull(value);

		if (value.isEmpty()) {
			throw new NullPointerException();
		}

		return value;
	}

	public int getUserHashSaltSize() {
		return userHashSaltSize;
	}

	public void setUserHashSaltSize(int userHashSaltSize) {
		this.userHashSaltSize = userHashSaltSize;
	}

	public int getUserHashIterations() {
		return userHashIterations;
	}

	public void setUserHashIterations(int userHashIterations) {
		this.userHashIterations = userHashIterations;
	}

	public String getUserHashAlgorithm() {
		return userHashAlgorithm;
	}

	public void setUserHashAlgorithm(String userHashAlgorithm) {
		this.userHashAlgorithm = userHashAlgorithm;
	}

	public String getUserKeyAlgorithm() {
		return userKeyAlgorithm;
	}

	public void setUserKeyAlgorithm(String userKeyAlgorithm) {
		this.userKeyAlgorithm = userKeyAlgorithm;
	}

	public int getUserKeySize() {
		return userKeySize;
	}

	public void setUserKeySize(int userKeySize) {
		this.userKeySize = userKeySize;
	}

	public String getUserPBEAlgorithm() {
		return userPBEAlgorithm;
	}

	public void setUserPBEAlgorithm(String userPBEAlgorithm) {
		this.userPBEAlgorithm = userPBEAlgorithm;
	}

	public String getWalletKeyAlgorithm() {
		return walletKeyAlgorithm;
	}

	public void setWalletKeyAlgorithm(String walletKeyAlgorithm) {
		this.walletKeyAlgorithm = walletKeyAlgorithm;
	}

	public int getWalletKeySize() {
		return walletKeySize;
	}

	public void setWalletKeySize(int walletKeySize) {
		this.walletKeySize = walletKeySize;
	}

}
