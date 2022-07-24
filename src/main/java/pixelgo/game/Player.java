package pixelgo.game;

public enum Player {
	/**
	 * The first player.
	 */
	BLACK,
	
	/**
	 * The second player.
	 */
	WHITE;
	
	static {
		BLACK.opponent = WHITE;
		WHITE.opponent = BLACK;
	}
	
	/**
	 * The player's opponent.
	 */
	public Player opponent;
}
