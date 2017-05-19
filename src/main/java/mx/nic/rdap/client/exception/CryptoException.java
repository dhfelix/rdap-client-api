package mx.nic.rdap.client.exception;

import mx.nic.rdap.client.wallet.Crypto;

/**
 * Exception to wrap exceptions thrown by {@link Crypto} functions.
 */
public class CryptoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6126383778410166367L;

	public CryptoException() {
		super();
	}

	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptoException(String message) {
		super(message);
	}

	public CryptoException(Throwable cause) {
		super(cause);
	}

}
