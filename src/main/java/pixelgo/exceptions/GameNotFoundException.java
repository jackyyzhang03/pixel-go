package pixelgo.exceptions;

public class GameNotFoundException extends ClientException {
	private static final long serialVersionUID = 1L;

	public GameNotFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GameNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GameNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GameNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public GameNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
