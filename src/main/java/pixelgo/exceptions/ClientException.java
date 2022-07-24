package pixelgo.exceptions;

/**
 * Exception thrown when the game client makes a bad request.
 * 
 * @author jacky
 *
 */
public class ClientException extends Exception {
	private static final long serialVersionUID = 1L;

	public ClientException() {
		super();
	}

	public ClientException(String message) {
		super(message);
	}

	public ClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClientException(Throwable cause) {
		super(cause);
	}

}
