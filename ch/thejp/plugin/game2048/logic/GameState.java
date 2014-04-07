package ch.thejp.plugin.game2048.logic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GameState implements IGameState {

	private byte[][] field = new byte[FIELD_SIZE][FIELD_SIZE];
	private long score = 0;
	private boolean gameOver = false;
	private boolean gameOverValid = true;

	/**
	 * Returns a 4x4 array of byte values, which represent the field
	 * @return 4x4 byte array
	 */
	public byte[][] getField() {
		return field;
	}

	@Override
	public byte getTile(int x, int y) {
		return field[x][y];
	}

	@Override
	public void setTile(int x, int y, byte tile){
		field[x][y] = tile;
		this.gameOverValid = false;
	}

	@Override
	public long getScore() {
		return score;
	}

	@Override
	public void setScore(long score){
		this.score = score;
	}

	@Override
	public boolean isGameOver() {
		if(!gameOverValid){ setGameOver(calculateGameOver()); }
		return gameOver;
	}

	@Override
	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
		this.gameOverValid = true;
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

	/**
	 * Calculates the gameover condition for the current field
	 * @return gameover state (true=game over)
	 */
	private boolean calculateGameOver(){
		for(int x = 0; x < FIELD_SIZE; ++x){
			for(int y = 0; y < FIELD_SIZE; ++y){
				if(field[x][y] <= 0){ return false; }
				else {
					
				}
			}
		}
		return false;
	}
}
