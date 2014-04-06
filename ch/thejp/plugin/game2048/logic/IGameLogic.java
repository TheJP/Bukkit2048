package ch.thejp.plugin.game2048.logic;

/**
 * Represents the logic of the 2048 game
 * The logic is modified a bit, so it can be represented in a mc inventory:
 * - The highest score possible is 64
 * - The game adds random Tiles with value 1 or 2 (instead of 2 or 4)
 * - The combination of two tiles results in the value incremented by one (1 and 1 => 2, 22 and 22 => 23)
 * @author JP
 *
 */
public interface IGameLogic {
	/**
	 * Returns the current gamestate
	 * @return
	 */
	IGameState getGameState();
	/**
	 * Sets the given gamestate
	 * @param state
	 */
	void setGameState(IGameState state);
	/**
	 * Make a gamemove in the given direction
	 * @param dir
	 */
	void move(Direction dir);
}
