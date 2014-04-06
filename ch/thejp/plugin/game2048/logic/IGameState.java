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
	 * Returns a 4x4 array of byte values, which represent the field
	 * @return 4x4 byte array
	 */
	byte[][] getField();
	/**
	 * Returns the current score
	 * @return score of the current game
	 */
	long getScore();
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
