package pixelgo.exceptions;

import pixelgo.game.Violation;

/**
 * Exception thrown when an invalid move is attempted to be made.
 *
 * @author jacky
 *
 */
public class InvalidMoveException extends ClientException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param violatedRule the rule violation
	 */
	public InvalidMoveException(Violation ruleViolation) {
		super(ruleViolation.toString());
	}
}
