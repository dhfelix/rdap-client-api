package mx.nic.rdap.client.wallet;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mx.nic.rdap.client.dao.exception.DataAccessException;
import mx.nic.rdap.client.dao.object.EncryptedWalletKey;
import mx.nic.rdap.client.dao.object.RdapClientUser;
import mx.nic.rdap.client.exception.ConfigurationException;
import mx.nic.rdap.client.exception.UserExistException;
import mx.nic.rdap.client.service.DataAccessService;
import mx.nic.rdap.client.spi.UserDAO;
import mx.nic.rdap.client.spi.WalletKeyDAO;

public class WalletModel {

	private static WalletConfiguration walletConfiguration;

	private static final String DEFAULT_PROPERTIES_FILE = "META-INF/default_wallet_conf.properties";

	private WalletModel() {
		// no code
	}

	private static Properties getDefaultProperties() throws IOException {
		Properties properties = new Properties();

		try (InputStream inStream = WalletModel.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
			properties.load(inStream);
		}

		return properties;
	}

	public static void initWallet(Properties properties) throws IOException, ConfigurationException {
		Properties defaultProperties = getDefaultProperties();
		defaultProperties.putAll(properties);

		walletConfiguration = new WalletConfiguration(defaultProperties);
	}

	public static WalletConfiguration getWalletConfiguration() {
		return walletConfiguration;
	}

	public static WalletUser createNewUser(String username, String password)
			throws NoSuchAlgorithmException, UserExistException, DataAccessException, InvalidKeySpecException,
			InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException {
		RdapClientUser user = UserModel.createUser(username, password);

		WalletKey walletKey = new WalletKey();
		PBEKey userPasswordKey = new PBEKey(password, user.getSalt(), user.getIterations(), user.getPbeAlgorithm(),
				user.getKeySize(), user.getKeyAlgorithm());

		EncryptedWalletKey encryptedKey = new EncryptedWalletKey(null, user.getId(),
				walletKey.getEncryptedWalletKey(userPasswordKey.getSecretKey()), walletKey.getCipherAlgorithm());

		WalletKeyDAO keyDAO = DataAccessService.getWalletKeyDAO();
		keyDAO.storeWalletKey(encryptedKey);

		return new WalletUser(user, encryptedKey, userPasswordKey.getSecretKey(), walletKey.getUserKey());
	}

	public static WalletUser updateUser(WalletUser walletUser, String oldPassword, String newPassword)
			throws DataAccessException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException {
		// get the user
		RdapClientUser user = walletUser.getUser();
		EncryptedWalletKey encryptedWalletKey = walletUser.getEncryptedWalletKey();

		// decrypt with old password
		PBEKey oldPBEKey = new PBEKey(oldPassword, user.getSalt(), user.getIterations(), user.getPbeAlgorithm(),
				user.getKeySize(), user.getKeyAlgorithm());
		WalletKey walletKey = new WalletKey(encryptedWalletKey.getEncryptedWalletKey(), oldPBEKey.getSecretKey(),
				encryptedWalletKey.getWalletKeyAlgorithm());

		// encrypt with the new password and update
		String base64PasswordHash = UserModel.getBase64PasswordHash(newPassword, user.getSalt(), user.getIterations(),
				user.getHashAlgorithm());
		user.setHashedPassword(base64PasswordHash);

		PBEKey newPBEKey = new PBEKey(newPassword, user.getSalt(), user.getIterations(), user.getPbeAlgorithm(),
				user.getKeySize(), user.getKeyAlgorithm());
		encryptedWalletKey.setEncryptedWalletKey(walletKey.getEncryptedWalletKey(newPBEKey.getSecretKey()));

		UserDAO userDao = DataAccessService.getUserDAO();
		WalletKeyDAO keyDAO = DataAccessService.getWalletKeyDAO();
		userDao.updateUser(user);
		keyDAO.updateWalletKey(encryptedWalletKey);

		return walletUser;
	}

	public static WalletUser getUser(String username, String password) throws DataAccessException,
			NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException {
		UserDAO userDAO = DataAccessService.getUserDAO();
		RdapClientUser user = userDAO.getUser(username);
		if (user == null || !UserModel.isValidPassword(password, user.getSalt(), user.getIterations(),
				user.getHashAlgorithm(), user.getHashedPassword())) {
			return null;
		}

		WalletKeyDAO keyDao = DataAccessService.getWalletKeyDAO();
		EncryptedWalletKey encryptedWalletKey = keyDao.getWalletKey(user.getId());

		PBEKey pbeKey = new PBEKey(password, user.getSalt(), user.getIterations(), user.getPbeAlgorithm(),
				user.getKeySize(), user.getKeyAlgorithm());

		WalletKey walletKey = new WalletKey(encryptedWalletKey.getEncryptedWalletKey(), pbeKey.getSecretKey(),
				encryptedWalletKey.getWalletKeyAlgorithm());

		return new WalletUser(user, encryptedWalletKey, pbeKey.getSecretKey(), walletKey.getUserKey());
	}

}
