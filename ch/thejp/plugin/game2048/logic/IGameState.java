package ch.thejp.plugin.game2048.logic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents a game state (Field, Score)
 * The gamestate is serializable with the methods read / write
 * @author JP
 *
 */
public interface IGameState {
	/**
	 * Size of a side of the quadratic game field
	 */
	public final int FIELD_SIZE = 4;
	/**
	 * Returns a 4x4 array of byte values, which represent the field
	 * @return 4x4 byte array
	 */
	byte[][] getField();
	/**
	 * Sets the tile at the specified position
	 * @param x X-Position
	 * @param y Y-Position
	 * @param tile Tile value
	 */
	public void setTile(int x, int y, byte tile);
	/**
	 * Returns the current score
	 * @return score of the current game
	 */
	long getScore();
	/**
	 * Sets the score to the new value
	 * @param score
	 */
	public void setScore(long score);
	/**
	 * Returns true if the game is over (no free tiles left)
	 * @return
	 */
	public boolean isGameOver();
	/**
	 * Sets whether the game is over
	 * @param gameOver true=game over, false=game not yet over
	 */
	public void setGameOver(boolean gameOver);
	/**
	 * Writes the gamestate to an OutputStream
	 * @param stream
	 */
	void write(DataOutput stream) throws IOException;
	/**
	 * Reads the gamestate from an InputStream
	 * @param stream
	 */
	void read(DataInput stream) throws IOException;
}
