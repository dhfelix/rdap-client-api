package mx.nic.rdap.client.bootstrap;

public class BootstrapException extends Exception {

	private static final long serialVersionUID = 1613124955302911611L;

	public BootstrapException() {
		super();
	}

	public BootstrapException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BootstrapException(String message, Throwable cause) {
		super(message, cause);
	}

	public BootstrapException(String message) {
		super(message);
	}

	public BootstrapException(Throwable cause) {
		super(cause);
	}

}
