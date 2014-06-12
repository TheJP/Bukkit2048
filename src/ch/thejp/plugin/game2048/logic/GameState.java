package ch.thejp.plugin.game2048.logic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GameState implements IGameState {

	private long[][] field = new long[FIELD_SIZE+2][FIELD_SIZE+2];
	private long score = 0;
	private boolean gameOver = false;
	private boolean gameOverValid = true;

	@Override
	public long getTile(int x, int y) {
		return field[x+1][y+1];
	}

	@Override
	public void setTile(int x, int y, long tile){
		field[x+1][y+1] = tile;
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
				stream.writeLong(getTile(x, y));
			}
		}
	}

	@Override
	public void read(DataInput stream) throws IOException {
		score = stream.readLong();
		//Read field
		for(int x = 0; x < FIELD_SIZE; ++x){
			for(int y = 0; y < FIELD_SIZE; ++y){
				setTile(x, y, stream.readLong());
			}
		}
	}

	/**
	 * Calculates the gameover condition for the current field
	 * @return gameover state (true=game over)
	 */
	private boolean calculateGameOver(){
		for(int x = 1; x <= FIELD_SIZE; ++x){
			for(int y = 1; y <= FIELD_SIZE; ++y){
				if(field[x][y] <= 0){ return false; }
				else if(
					field[x-1][y] == field[x][y] ||
				    field[x+1][y] == field[x][y] ||
				    field[x][y-1] == field[x][y] ||
				    field[x][y+1] == field[x][y]
				){
					return false;
				}
			}
		}
		return true;
	}
}
