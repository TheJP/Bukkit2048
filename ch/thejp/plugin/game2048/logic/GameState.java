package ch.thejp.plugin.game2048.logic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GameState implements IGameState {

	/**
	 * Size of a side of the quadratic game field
	 */
	public final int FIELD_SIZE = 4;
	private byte[][] field = new byte[FIELD_SIZE][FIELD_SIZE];
	private long score = 0;

	@Override
	public byte[][] getField() {
		return field;
	}

	/**
	 * Sets the tile at the specified position
	 * @param x X-Position
	 * @param y Y-Position
	 * @param tile Tile value
	 */
	public void setTile(int x, int y, byte tile){
		field[x][y] = tile;
	}

	@Override
	public long getScore() {
		return score;
	}
	
	/**
	 * Sets the score to the new value
	 * @param score
	 */
	public void setScore(long score){
		this.score = score;
	}

	@Override
	public void write(DataOutput stream) throws IOException {
		stream.writeLong(score);
		//Write field
		for(int x = 0; x < FIELD_SIZE; ++x){
			for(int y = 0; y < FIELD_SIZE; ++y){
				stream.writeByte(field[x][y]);
			}
		}
	}

	@Override
	public void read(DataInput stream) throws IOException {
		score = stream.readLong();
		//Read field
		for(int x = 0; x < FIELD_SIZE; ++x){
			for(int y = 0; y < FIELD_SIZE; ++y){
				field[x][y] = stream.readByte();
			}
		}
	}

}
