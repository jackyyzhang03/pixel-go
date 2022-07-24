package pixelgo.exceptions;

/**
 * Exception thrown when a client attempts to join a spot that is already filled.
 * 
 * @author jacky
 *
 */
public class SpotFullException extends ClientException {
	private static final long serialVersionUID = 1L;

	public SpotFullException() {
		super();
	}

	public SpotFullException(String message) {
		super(message);
	}

	public SpotFullException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpotFullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SpotFullException(Throwable cause) {
		super(cause);
	}

}
