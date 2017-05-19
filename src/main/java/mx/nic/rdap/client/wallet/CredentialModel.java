package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mx.nic.rdap.client.dao.exception.DataAccessException;
import mx.nic.rdap.client.dao.object.EncryptedCredential;
import mx.nic.rdap.client.exception.CryptoException;
import mx.nic.rdap.client.service.DataAccessService;
import mx.nic.rdap.client.spi.CredentialDAO;

public class CredentialModel {

	private CredentialModel() {
		// No code
	}

	public static List<RdapCredential> getCredentialForUserAndServer(User user, String serverId)
			throws DataAccessException, CryptoException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		List<EncryptedCredential> credentialsForRdapServer = dao
				.getCredentialsForRdapServer(user.getWalletUser().getId(), serverId);

		if (credentialsForRdapServer == null || credentialsForRdapServer.isEmpty()) {
			return Collections.emptyList();
		}

		List<RdapCredential> credentials = new ArrayList<>();
		for (EncryptedCredential enc : credentialsForRdapServer) {
			String decryptUserCredentialPassword;
			try {
				decryptUserCredentialPassword = Crypto.decryptUserCredentialPassword(enc.getEncryptedPassword(),
						user.getWalletUser().getCipherAlgorithm(), user.getUserWalletKey());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
					| NoSuchPaddingException e) {
				throw new CryptoException(e);
			}
			RdapCredential rdapCredential = new RdapCredential(enc.getId(), enc.getUsername(),
					decryptUserCredentialPassword, enc.getRdapServerId());
			credentials.add(rdapCredential);
		}

		return credentials;
	}

	public static List<RdapCredential> getAllForUser(User user) throws DataAccessException, CryptoException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		List<EncryptedCredential> credentialsForRdapServer = dao.getCredentials(user.getWalletUser().getId());

		if (credentialsForRdapServer == null || credentialsForRdapServer.isEmpty()) {
			return Collections.emptyList();
		}

		List<RdapCredential> credentials = new ArrayList<>();
		for (EncryptedCredential enc : credentialsForRdapServer) {
			String decryptUserCredentialPassword;
			try {
				decryptUserCredentialPassword = Crypto.decryptUserCredentialPassword(enc.getEncryptedPassword(),
						user.getWalletUser().getCipherAlgorithm(), user.getUserWalletKey());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
					| NoSuchPaddingException e) {
				throw new CryptoException(e);
			}
			RdapCredential rdapCredential = new RdapCredential(enc.getId(), enc.getUsername(),
					decryptUserCredentialPassword, enc.getRdapServerId());
			credentials.add(rdapCredential);
		}

		return credentials;
	}

	public static void deleteCredential(User user, RdapCredential credential) throws DataAccessException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();
		dao.deleteCredential(user.getWalletUser().getId(), credential.getId());
	}

	public static void updateCredentials(User user, RdapCredential credential)
			throws DataAccessException, CryptoException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();

		String encryptUserCredentialPassword;
		try {
			encryptUserCredentialPassword = Crypto.encryptUserCredentialPassword(credential.getPassword(),
					user.getWalletUser().getCipherAlgorithm(), user.getUserWalletKey());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new CryptoException(e);
		}

		EncryptedCredential encryptedCredential = new EncryptedCredential();
		encryptedCredential.setId(credential.getId());
		encryptedCredential.setUserId(user.getWalletUser().getId());
		encryptedCredential.setRdapServerId(credential.getServerId());
		encryptedCredential.setEncryptedPassword(encryptUserCredentialPassword);
		encryptedCredential.setUsername(credential.getUsername());
		dao.updateCredential(encryptedCredential);
	}

	public static void insertCredential(User user, RdapCredential credential)
			throws DataAccessException, CryptoException {
		CredentialDAO dao = DataAccessService.getCredentialDAO();

		String encryptUserCredentialPassword;
		try {
			encryptUserCredentialPassword = Crypto.encryptUserCredentialPassword(credential.getPassword(),
					user.getWalletUser().getCipherAlgorithm(), user.getUserWalletKey());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new CryptoException(e);
		}

		EncryptedCredential encryptedCredential = new EncryptedCredential();
		encryptedCredential.setId(credential.getId());
		encryptedCredential.setUserId(user.getWalletUser().getId());
		encryptedCredential.setRdapServerId(credential.getServerId());
		encryptedCredential.setEncryptedPassword(encryptUserCredentialPassword);
		encryptedCredential.setUsername(credential.getUsername());
		dao.storeCredential(encryptedCredential);
	}

}
