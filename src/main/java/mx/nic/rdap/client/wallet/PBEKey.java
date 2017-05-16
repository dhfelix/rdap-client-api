package mx.nic.rdap.client.wallet;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class PBEKey {

	private SecretKey secretKey;

	public PBEKey(String plainPassword, String salt, int iterations, String pbeAlgorithm, int keySize,
			String keyAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgorithm);
		KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), DatatypeConverter.parseHexBinary(salt), iterations,
				keySize);
		SecretKey generateSecret = factory.generateSecret(spec);
		secretKey = new SecretKeySpec(generateSecret.getEncoded(), keyAlgorithm);
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	// public static boolean isValidPassword(String plainPassword, String salt,
	// int iterations, String hashAlgorithm,
	// String storedPasswordHash) throws NoSuchAlgorithmException {
	//
	// String base64PasswordHash = getBase64PasswordHash(plainPassword, salt,
	// iterations, hashAlgorithm);
	// return base64PasswordHash.equals(storedPasswordHash);
	// }
	//
	// public static String getBase64PasswordHash(String plainPassword, String
	// salt, int iterations, String hashAlgorithm)
	// throws NoSuchAlgorithmException {
	// MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
	//
	// byte[] saltBinary = DatatypeConverter.parseHexBinary(salt);
	// md.update(saltBinary);
	//
	// byte[] hash = md.digest(plainPassword.getBytes());
	//
	// for (int i = 0; i < iterations; i++) {
	// md.update(saltBinary);
	// hash = md.digest(hash);
	// }
	//
	// return Base64.getEncoder().encodeToString(hash);
	// }
}
