package mx.nic.rdap.client.wallet;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

	private Crypto() {
		// no code
	}

	public static SecretKey getPBESecretKey(String plainPassword, String pbeAlgorithm, byte[] salt, int iterations,
			int keySize, String keyAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgorithm);
		PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, keySize);
		SecretKey generateSecret = factory.generateSecret(spec);
		return new SecretKeySpec(generateSecret.getEncoded(), keyAlgorithm);
	}

	public static SecretKey createNewKey(String walletKeyAlgorithm, int keySize) throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance(walletKeyAlgorithm);

		generator.init(keySize);

		return generator.generateKey();
	}

	public static byte[] getRandomSalt(int saltSize) {
		byte[] salt = new byte[saltSize];

		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);

		return salt;
	}

	// public static String getBase64PasswordHash(String plainPassword, String
	// salt, int iterations, String hashAlgorithm)
	// throws NoSuchAlgorithmException {
	// return getBase64PasswordHash(plainPassword,
	// DatatypeConverter.parseHexBinary(salt), iterations, hashAlgorithm);
	// }

	public static String getBase64PasswordHash(String plainPassword, byte[] saltBinary, int iterations,
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

	public static String getBase64EncryptedWalletSecretKey(SecretKey passwordBaseKey, SecretKey keyToBeEncrypted,
			String cipherAlgorithm)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.WRAP_MODE, passwordBaseKey);
		byte[] wrap = cipher.wrap(keyToBeEncrypted);

		return Base64.getEncoder().encodeToString(wrap);
	}

	public static SecretKey getWalletSecretKey(SecretKey pbeKey, String encryptedSecretKey, String cipherAlgorithm,
			String keyAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.UNWRAP_MODE, pbeKey);
		byte[] wrappedKey = Base64.getDecoder().decode(encryptedSecretKey);
		SecretKey key = (SecretKey) cipher.unwrap(wrappedKey, keyAlgorithm, Cipher.SECRET_KEY);
		return key;
	}

	public static String decryptUserCredentialPassword(String encryptedPass, String cipherAlgorithm, SecretKey userKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, userKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPass));
		return new String(decryptedBytes);
	}

	public static String encryptUserCredentialPassword(String plainPass, String cipherAlgorithm, SecretKey userKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, userKey);
		byte[] doFinal = cipher.doFinal(plainPass.getBytes());
		return Base64.getEncoder().encodeToString(doFinal);
	}

}
