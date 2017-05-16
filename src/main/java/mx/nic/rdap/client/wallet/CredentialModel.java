package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import mx.nic.rdap.client.dao.exception.DataAccessException;
import mx.nic.rdap.client.dao.object.EncryptedCredential;
import mx.nic.rdap.client.service.DataAccessService;
import mx.nic.rdap.client.spi.CredentialDAO;

public class CredentialModel {

	private CredentialModel() {
		// No code
	}

	private static String decryptUserCredentialPassword(String encryptedPass, String cipherAlgorithm, SecretKey userKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, userKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPass));
		return new String(decryptedBytes);
	}

	private static String encryptUserCredentialPassword(String plainPass, String cipherAlgorithm, SecretKey userKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, userKey);
		byte[] doFinal = cipher.doFinal(plainPass.getBytes());
		return Base64.getEncoder().encodeToString(doFinal);
	}

	public static List<RdapCredential> getCredentialForUserAndServer(WalletUser user, String serverId)
			throws DataAccessException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		List<EncryptedCredential> credentialsForRdapServer = dao.getCredentialsForRdapServer(user.getUser().getId(),
				serverId);

		if (credentialsForRdapServer == null || credentialsForRdapServer.isEmpty()) {
			return Collections.emptyList();
		}

		List<RdapCredential> credentials = new ArrayList<>();
		for (EncryptedCredential enc : credentialsForRdapServer) {
			String decryptUserCredentialPassword = decryptUserCredentialPassword(enc.getEncryptedPassword(),
					user.getEncryptedWalletKey().getWalletKeyAlgorithm(), user.getUserWalletKey());
			RdapCredential rdapCredential = new RdapCredential(enc.getId(), enc.getUsername(),
					decryptUserCredentialPassword, enc.getRdapServerId());
			credentials.add(rdapCredential);
		}

		return credentials;
	}

	public static List<RdapCredential> getAllForUser(WalletUser user) throws DataAccessException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		List<EncryptedCredential> credentialsForRdapServer = dao.getCredentials(user.getUser().getId());

		if (credentialsForRdapServer == null || credentialsForRdapServer.isEmpty()) {
			return Collections.emptyList();
		}

		List<RdapCredential> credentials = new ArrayList<>();
		for (EncryptedCredential enc : credentialsForRdapServer) {
			String decryptUserCredentialPassword = decryptUserCredentialPassword(enc.getEncryptedPassword(),
					user.getEncryptedWalletKey().getWalletKeyAlgorithm(), user.getUserWalletKey());
			RdapCredential rdapCredential = new RdapCredential(enc.getId(), enc.getUsername(),
					decryptUserCredentialPassword, enc.getRdapServerId());
			credentials.add(rdapCredential);
		}

		return credentials;
	}

	public static void deleteCredential(WalletUser user, RdapCredential credential) throws DataAccessException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		dao.deleteCredential(user.getUser().getId(), credential.getId());
	}

	public static void updateCredentials(WalletUser user, RdapCredential credential)
			throws DataAccessException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();

		String encryptUserCredentialPassword = encryptUserCredentialPassword(credential.getPassword(),
				user.getEncryptedWalletKey().getWalletKeyAlgorithm(), user.getUserWalletKey());

		EncryptedCredential encryptedCredential = new EncryptedCredential();
		encryptedCredential.setId(credential.getId());
		encryptedCredential.setUserId(user.getUser().getId());
		encryptedCredential.setRdapServerId(credential.getServerId());
		encryptedCredential.setEncryptedPassword(encryptUserCredentialPassword);
		encryptedCredential.setUsername(credential.getUsername());
		dao.updateCredential(encryptedCredential);
	}

	public static void insertCredential(WalletUser user, RdapCredential credential)
			throws DataAccessException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();

		String encryptUserCredentialPassword = encryptUserCredentialPassword(credential.getPassword(),
				user.getEncryptedWalletKey().getWalletKeyAlgorithm(), user.getUserWalletKey());

		EncryptedCredential encryptedCredential = new EncryptedCredential();
		encryptedCredential.setId(credential.getId());
		encryptedCredential.setUserId(user.getUser().getId());
		encryptedCredential.setRdapServerId(credential.getServerId());
		encryptedCredential.setEncryptedPassword(encryptUserCredentialPassword);
		encryptedCredential.setUsername(credential.getUsername());
		dao.storeCredential(encryptedCredential);
	}

}
