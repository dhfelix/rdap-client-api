package mx.nic.rdap.client.exception;

public class UserExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7018738989734306774L;

	public UserExistException() {
		super();
	}

	public UserExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserExistException(String message) {
		super(message);
	}

	public UserExistException(Throwable cause) {
		super(cause);
	}

}
