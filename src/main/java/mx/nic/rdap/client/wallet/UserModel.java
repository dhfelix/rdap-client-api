package mx.nic.rdap.client.wallet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

import mx.nic.rdap.client.dao.exception.DataAccessException;
import mx.nic.rdap.client.dao.object.RdapClientUser;
import mx.nic.rdap.client.exception.UserExistException;
import mx.nic.rdap.client.service.DataAccessService;
import mx.nic.rdap.client.spi.UserDAO;

public class UserModel {

	private UserModel() {
		// no code
	}

	public static boolean existUser(String username) throws DataAccessException {
		return DataAccessService.getUserDAO().existUser(username);
	}

	public static RdapClientUser createUser(String username, String password)
			throws UserExistException, DataAccessException, NoSuchAlgorithmException {
		UserDAO userDAO = DataAccessService.getUserDAO();
		boolean existUser = userDAO.existUser(username);
		if (existUser) {
			throw new UserExistException(username);
		}

		RdapClientUser user = createUserObject(username, password);
		userDAO.storeUser(user);

		return user;
	}

	private static RdapClientUser createUserObject(String username, String password) throws NoSuchAlgorithmException {
		byte[] salt = getRandomSalt(WalletModel.getWalletConfiguration().getUserHashSaltSize());
		int iterations = WalletModel.getWalletConfiguration().getUserHashIterations();
		String hashAlgorithm = WalletModel.getWalletConfiguration().getUserHashAlgorithm();
		String hashedPassword = getBase64PasswordHash(password, salt, iterations, hashAlgorithm);

		String keyAlgorithm = WalletModel.getWalletConfiguration().getUserKeyAlgorithm();
		int keySize = WalletModel.getWalletConfiguration().getUserKeySize();
		String pbeAlgorithm = WalletModel.getWalletConfiguration().getUserPBEAlgorithm();

		return new RdapClientUser(null, username, hashedPassword, DatatypeConverter.printHexBinary(salt), iterations,
				hashAlgorithm, keyAlgorithm, keySize, pbeAlgorithm);
	}

	private static byte[] getRandomSalt(int saltSize) {
		byte[] salt = new byte[saltSize];

		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);

		return salt;
	}

	public static boolean isValidPassword(String plainPassword, String salt, int iterations, String hashAlgorithm,
			String storedPasswordHash) throws NoSuchAlgorithmException {

		String base64PasswordHash = getBase64PasswordHash(plainPassword, salt, iterations, hashAlgorithm);
		return base64PasswordHash.equals(storedPasswordHash);
	}

	public static String getBase64PasswordHash(String plainPassword, String salt, int iterations, String hashAlgorithm)
			throws NoSuchAlgorithmException {
		return getBase64PasswordHash(plainPassword, DatatypeConverter.parseHexBinary(salt), iterations, hashAlgorithm);
	}

	private static String getBase64PasswordHash(String plainPassword, byte[] saltBinary, int iterations,
			String hashAlgorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

		md.update(saltBinary);

		byte[] hash = md.digest(plainPassword.getBytes());

		for (int i = 0; i < iterations; i++) {
			md.update(saltBinary);
			hash = md.digest(hash);
		}

		return Base64.getEncoder().encodeToString(hash);
	}

}
