package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import mx.nic.rdap.client.api.Configuration;
import mx.nic.rdap.client.dao.exception.DataAccessException;
import mx.nic.rdap.client.dao.object.WalletUser;
import mx.nic.rdap.client.exception.ConfigurationException;
import mx.nic.rdap.client.exception.CryptoException;
import mx.nic.rdap.client.exception.UserExistException;
import mx.nic.rdap.client.service.DataAccessService;
import mx.nic.rdap.client.spi.WalletUserDAO;

public class WalletModel {
	private static WalletConfiguration walletConfiguration;

	public static void initWallet(Properties properties) throws ConfigurationException {
		walletConfiguration = new WalletConfiguration(Configuration.getConfiguration());
	}

	public static WalletConfiguration getWalletConfiguration() {
		return walletConfiguration;
	}

	private WalletModel() {
		// no code
	}

	public static User insertNewUser(String username, String password)
			throws DataAccessException, CryptoException, UserExistException {
		if (existUser(username)) {
			throw new UserExistException("The username '" + username + "' already exists");
		}
		User user;
		try {
			user = createNewUser(username, password);
		} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | NoSuchPaddingException
				| IllegalBlockSizeException e) {
			throw new CryptoException(e);
		}

		WalletUserDAO dao = DataAccessService.getWalletUserDAO();
		long userId = dao.store(user.getWalletUser());
		user.getWalletUser().setId(userId);

		return user;
	}

	private static User createNewUser(String username, String password) throws NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException {
		WalletUser walletUser = new WalletUser();

		String userHashAlgorithm = getWalletConfiguration().getUserHashAlgorithm();
		int userIterations = getWalletConfiguration().getUserIterations();
		int userKeySize = getWalletConfiguration().getUserKeySize();
		String userPBEAlgorithm = getWalletConfiguration().getUserPBEAlgorithm();
		int userSaltSize = getWalletConfiguration().getUserSaltSize();
		String walletCipherAlgorithm = getWalletConfiguration().getWalletCipherAlgorithm();
		String walletKeyAlgorithm = getWalletConfiguration().getWalletKeyAlgorithm();

		byte[] saltBytes = Crypto.getRandomSalt(userSaltSize);
		String salt = DatatypeConverter.printHexBinary(saltBytes);

		String base64PasswordHash = Crypto.getBase64PasswordHash(password, saltBytes, userIterations,
				userHashAlgorithm);

		SecretKey secretKey = Crypto.createNewKey(getWalletConfiguration().getWalletKeyAlgorithm(),
				getWalletConfiguration().getUserKeySize());
		SecretKey pbeSecretKey = Crypto.getPBESecretKey(password, userPBEAlgorithm, saltBytes, userIterations,
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
		walletUser.setUsername(username);

		User newUser = new User(walletUser, pbeSecretKey, secretKey);

		return newUser;
	}

	public static User getUser(String username, String password) throws DataAccessException, CryptoException {
		WalletUserDAO dao = DataAccessService.getWalletUserDAO();

		WalletUser user = dao.getByUsername(username);
		if (user == null || !isValidPassword(password, user.getSalt(), user.getIterations(), user.getHashAlgorithm(),
				user.getHashedPassword())) {
			return null;
		}

		// get User PBE
		SecretKey pbeSecretKey;
		try {
			pbeSecretKey = Crypto.getPBESecretKey(password, user.getPbeAlgorithm(),
					DatatypeConverter.parseHexBinary(user.getSalt()), user.getIterations(), user.getKeySize(),
					user.getKeyAlgorithm());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}
		// get User wallet key
		SecretKey walletSecretKey;
		try {
			walletSecretKey = Crypto.getWalletSecretKey(pbeSecretKey, user.getEncryptedWalletKey(),
					user.getCipherAlgorithm(), user.getKeyAlgorithm());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new CryptoException(e);
		}

		return new User(user, pbeSecretKey, walletSecretKey);
	}

	private static boolean isValidPassword(String plainPassword, String salt, int iterations, String hashAlgorithm,
			String storedPasswordHash) throws CryptoException {

		String base64PasswordHash;
		try {
			base64PasswordHash = Crypto.getBase64PasswordHash(plainPassword, DatatypeConverter.parseHexBinary(salt),
					iterations, hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}
		return base64PasswordHash.equals(storedPasswordHash);
	}

	public static User updatePassword(User user, String oldPassword, String newPassword)
			throws DataAccessException, CryptoException {
		// get the user
		WalletUser wUser = user.getWalletUser();

		byte[] salt = DatatypeConverter.parseHexBinary(wUser.getSalt());
		// verify the oldPassword
		boolean validPassword = isValidPassword(oldPassword, wUser.getSalt(), wUser.getIterations(),
				wUser.getHashAlgorithm(), wUser.getHashedPassword());
		if (!validPassword) {
			return null;
		}

		// updates new password hash
		try {
			String base64PasswordHash = Crypto.getBase64PasswordHash(newPassword, salt, wUser.getIterations(),
					wUser.getHashAlgorithm());
			wUser.setHashedPassword(base64PasswordHash);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(e);
		}

		// encrypt the wallet secret key with new password
		try {
			SecretKey pbeSecretKey = Crypto.getPBESecretKey(newPassword, wUser.getPbeAlgorithm(), salt,
					wUser.getIterations(), wUser.getKeySize(), wUser.getKeyAlgorithm());
			user.setPbeKey(pbeSecretKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoException(e);
		}

		// updates new encrypted wallet secret key
		try {
			String base64EncryptedWalletSecretKey = Crypto.getBase64EncryptedWalletSecretKey(user.getPbeKey(),
					user.getUserWalletKey(), wUser.getCipherAlgorithm());
			wUser.setEncryptedWalletKey(base64EncryptedWalletSecretKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException e) {
			throw new CryptoException(e);
		}

		WalletUserDAO dao = DataAccessService.getWalletUserDAO();
		dao.update(wUser);

		return user;
	}

	public static boolean existUser(String username) throws DataAccessException {
		WalletUserDAO dao = DataAccessService.getWalletUserDAO();
		return dao.existByUsername(username);
	}
}
