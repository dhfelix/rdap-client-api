package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class WalletKey {

	private String cipherAlgorithm;

	private SecretKey userKey;

	public WalletKey(String encriptedKey, SecretKey passwordBaseKey, String cipherAlgorithm)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		this.cipherAlgorithm = cipherAlgorithm;
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.UNWRAP_MODE, passwordBaseKey);
		byte[] wrappedKey = Base64.getDecoder().decode(encriptedKey);
		userKey = (SecretKey) cipher.unwrap(wrappedKey, cipherAlgorithm, Cipher.SECRET_KEY);

	}

	public WalletKey() throws NoSuchAlgorithmException {
		cipherAlgorithm = WalletModel.getWalletConfiguration().getWalletKeyAlgorithm();
		KeyGenerator generator = KeyGenerator.getInstance(cipherAlgorithm);

		generator.init(WalletModel.getWalletConfiguration().getWalletKeySize());

		userKey = generator.generateKey();
	}

	public String decryptUserCredentialPassword(String encryptedPass) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, userKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPass));
		return new String(decryptedBytes);
	}

	public String encryptUserCredentialPassword(String plainPass) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, userKey);
		byte[] doFinal = cipher.doFinal(plainPass.getBytes());
		return Base64.getEncoder().encodeToString(doFinal);
	}

	public String getEncryptedWalletKey(SecretKey passwordBaseKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.WRAP_MODE, passwordBaseKey);
		byte[] wrap = cipher.wrap(userKey);

		return Base64.getEncoder().encodeToString(wrap);
	}

	public SecretKey getUserKey() {
		return userKey;
	}

	public String getCipherAlgorithm() {
		return cipherAlgorithm;
	}

	public void setCipherAlgorithm(String cipherAlgorithm) {
		this.cipherAlgorithm = cipherAlgorithm;
	}

}
