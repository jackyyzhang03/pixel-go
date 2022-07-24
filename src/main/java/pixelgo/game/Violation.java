package pixelgo.game;

public enum Violation {
	/**
	 * Position is already occupied by another stone.
	 */
	OCCUPIED_POSITION,
	
	/**
	 * The move would cause a repeat of a previous position reached in the game.
	 */
	REPEATED_POSITION,
	
	/**
	 * The move would cause the player's own stones to be removed.
	 */
	SUICIDE,
	
	
	/**
	 * The move contains an illegal argument that could not be parsed.
	 */
	ILLEGAL_ARGUMENT
}
