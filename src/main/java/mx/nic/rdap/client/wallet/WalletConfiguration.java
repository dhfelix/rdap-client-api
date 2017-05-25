package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import mx.nic.rdap.client.dao.object.WalletUser;
import mx.nic.rdap.client.exception.ConfigurationException;
import mx.nic.rdap.client.exception.CryptoException;

public class WalletConfiguration {

	private final static String DUMMY_PASS = "validation dummy password";
	private final static String DUMMY_USERNAME = "dummy username";

	private static final String USER_HASH_SALT_SIZE = "user.hash_salt_size";
	private static final String USER_HASH_ITERATIONS = "user.hash_iterations";
	private static final String USER_HASH_ALGORITHM = "user.hash_algorithm";
	private static final String USER_KEY_SIZE = "user.key_size";
	private static final String USER_PBE_ALGORITHM = "user.pbe_algorithm";

	private static final String WALLET_KEY_ALGORITHM = "wallet.key_algorithm";
	private static final String WALLET_CIPHER_ALGORITHM = "wallet.cipher_algorithm";

	private int userSaltSize;
	private int userIterations;
	private String userHashAlgorithm;
	private int userKeySize;
	private String userPBEAlgorithm;
	private String walletKeyAlgorithm;
	private String walletCipherAlgorithm;

	public WalletConfiguration(Properties properties) throws ConfigurationException {
		List<String> invalidProperties = new ArrayList<>();

		String key = USER_HASH_SALT_SIZE;
		try {
			userSaltSize = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		key = USER_HASH_ITERATIONS;
		try {
			userIterations = getInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			invalidProperties.add(key);
		}

		key = USER_HASH_ALGORITHM;
		try {
			userHashAlgorithm = getString(properties.getProperty(key));
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

		key = WALLET_CIPHER_ALGORITHM;
		try {
			walletCipherAlgorithm = getString(properties.getProperty(key));
		} catch (NullPointerException e) {
			invalidProperties.add(key);
		}
		if (!invalidProperties.isEmpty()) {
			throw new ConfigurationException("Invalid properties : " + invalidProperties.toString());
		}

		try {
			validateWalletConfig();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| IllegalBlockSizeException | CryptoException e) {
			throw new ConfigurationException(
					"It looks like it is an invalid property or is not available for encryption or hashing.", e);
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

	public int getUserSaltSize() {
		return userSaltSize;
	}

	public void setUserSaltSize(int userHashSaltSize) {
		this.userSaltSize = userHashSaltSize;
	}

	public int getUserIterations() {
		return userIterations;
	}

	public void setUserIterations(int userHashIterations) {
		this.userIterations = userHashIterations;
	}

	public String getUserHashAlgorithm() {
		return userHashAlgorithm;
	}

	public void setUserHashAlgorithm(String userHashAlgorithm) {
		this.userHashAlgorithm = userHashAlgorithm;
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

	public String getWalletCipherAlgorithm() {
		return walletCipherAlgorithm;
	}

	public void setWalletCipherAlgorithm(String walletCipherAlgorithm) {
		this.walletCipherAlgorithm = walletCipherAlgorithm;
	}

	private void validateWalletConfig() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException, CryptoException {
		WalletUser walletUser = new WalletUser();

		byte[] saltBytes = Crypto.getRandomSalt(userSaltSize);
		String salt = DatatypeConverter.printHexBinary(saltBytes);

		String base64PasswordHash = Crypto.getBase64PasswordHash(DUMMY_PASS, saltBytes, userIterations,
				userHashAlgorithm);

		SecretKey secretKey = Crypto.createNewKey(walletKeyAlgorithm, userKeySize);
		SecretKey pbeSecretKey = Crypto.getPBESecretKey(DUMMY_PASS, userPBEAlgorithm, saltBytes, userIterations,
				userKeySize, walletKeyAlgorithm);

		String base64EncryptedWalletSecretKey = Crypto.getBase64EncryptedWalletSecretKey(pbeSecretKey, secretKey,
				walletCipherAlgorithm);

		walletUser.setCipherAlgorithm(walletCipherAlgorithm);
		walletUser.setEncryptedWalletKey(base64EncryptedWalletSecretKey);
		walletUser.setHashAlgorithm(userHashAlgorithm);
		walletUser.setHashedPassword(base64PasswordHash);
		walletUser.setIterations(userIterations);
		walletUser.setKeyAlgorithm(walletKeyAlgorithm);
		walletUser.setKeySize(userKeySize);
		walletUser.setPbeAlgorithm(userPBEAlgorithm);
		walletUser.setSalt(salt);
		walletUser.setUsername(DUMMY_USERNAME);

		String otherBase64PasswordHash = Crypto.getBase64PasswordHash(DUMMY_PASS,
				DatatypeConverter.parseHexBinary(walletUser.getSalt()), walletUser.getIterations(),
				walletUser.getHashAlgorithm());

		if (!base64PasswordHash.equals(otherBase64PasswordHash)) {
			throw new CryptoException("Password hash are differents");
		}

		// get User PBE
		SecretKey otherPbeSecretKey = Crypto.getPBESecretKey(DUMMY_PASS, walletUser.getPbeAlgorithm(),
				DatatypeConverter.parseHexBinary(walletUser.getSalt()), walletUser.getIterations(),
				walletUser.getKeySize(), walletUser.getKeyAlgorithm());
		if (!otherPbeSecretKey.equals(pbeSecretKey)) {
			throw new CryptoException("PBEKeys are differents");
		}

		// get User wallet key
		SecretKey otherWalletSecretKey = Crypto.getWalletSecretKey(otherPbeSecretKey,
				walletUser.getEncryptedWalletKey(), walletUser.getCipherAlgorithm(), walletUser.getKeyAlgorithm());
		if (!otherWalletSecretKey.equals(secretKey)) {
			throw new CryptoException("Wallet keys are differents");
		}

		walletUser = null;

	}
}
