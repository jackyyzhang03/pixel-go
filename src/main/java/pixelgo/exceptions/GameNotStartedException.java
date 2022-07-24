package pixelgo.exceptions;

/**
 * Exception thrown when the client attempts to modify a game that has not
 * started.
 * 
 * @author jacky
 *
 */
public class GameNotStartedException extends ClientException {
	private static final long serialVersionUID = 1L;

	public GameNotStartedException() {
		super();
	}

	public GameNotStartedException(String message) {
		super(message);
	}

	public GameNotStartedException(String message, Throwable cause) {
		super(message, cause);
	}

	public GameNotStartedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GameNotStartedException(Throwable cause) {
		super(cause);
	}

}
