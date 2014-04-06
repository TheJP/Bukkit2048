package ch.thejp.plugin.game2048.logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic implements IGameLogic {

	/**
	 * Class which represents a moving strategy to keep the moving algorithm more generic
	 * @author JP
	 */
	private static class Strategy {
		public int initX, initY, smallStepX, smallStepY, bigStepX, bigStepY, endX, endY;
		public Strategy(int initX, int initY, int smallStepX, int smallStepY) {
			this.initX = initX;
			this.initY = initY;
			this.smallStepX = smallStepX;
			this.smallStepY = smallStepY;
			this.bigStepX = 1 - Math.abs(smallStepX);
			this.bigStepY = 1 - Math.abs(smallStepY);
			this.endX = (GameState.FIELD_SIZE - 1) - initX;
			this.endY = (GameState.FIELD_SIZE - 1) - initY;
		}
	}

	private IGameState gameState;
	private Strategy[] strategies = {
		new Strategy(0, 0, 0, 1), //UP
		new Strategy(GameState.FIELD_SIZE - 1, 0, -1, 0), //RIGHT
		new Strategy(0, GameState.FIELD_SIZE - 1, 0, -1), //DOWN
		new Strategy(0, 0, 1, 0) //LEFT
	};
	
	/**
	 * Constructor needs valid gamestate
	 * @param gameState
	 */
	public GameLogic(IGameState gameState) {
		this.gameState = gameState;
		addRandomTile(); //Add initial tile
	}

	@Override
	public IGameState getGameState() {
		return gameState;
	}

	@Override
	public void setGameState(IGameState state) {
		this.gameState = state;
	}

	/**
	 * Gets an Strategy version of the Direction
	 * @param dir
	 * @return
	 */
	private Strategy getMovingStrategy(Direction dir) {
		switch(dir){
		case Up:    return strategies[0];
		case Right: return strategies[1];
		case Down:  return strategies[2];
		case Left:  return strategies[3];
		default:    return strategies[0];
		}
	}

	private boolean isNotEnd(int i, int limit) {
		return (limit <= 0 && i >= limit) || (limit > 0 && i <= limit);
	}

	/**
	 * Add a 1 or a 2 tile at a random free location.
	 * The method returns false if no such tile could be added
	 * @return true=success
	 */
	private void addRandomTile(){
		List<Point> freeSpaces = new ArrayList<Point>();
		for(int x = 0; x < GameState.FIELD_SIZE; ++x){
			for(int y = 0; y < GameState.FIELD_SIZE; ++y){
				if(gameState.getField()[x][y] == 0){
					freeSpaces.add(new Point(x, y));
				}
			}
		}
		if(freeSpaces.size() <= 0){ return; }
		Random r = new Random();
		Point choosen = freeSpaces.get(r.nextInt(freeSpaces.size()));
		gameState.setTile(choosen.x, choosen.y, (byte) (r.nextInt(2)+1));
	}

	@Override
	public void move(Direction dir) {
		if(gameState.isGameOver()){ return; }
		//The main moving and scoring algorithm is defined here
		Strategy s = getMovingStrategy(dir);
		int tileX, tileY;
		boolean cond;
		boolean moved = false;
		for(
			int x = s.initX, y = s.initY;
			isNotEnd(x, s.endX) || isNotEnd(y, s.endY);
			x+=s.bigStepX, y+=s.bigStepY
		) {
			if(s.smallStepX != 0){ x = s.initX; }
			if(s.smallStepY != 0){ y = s.initY; }
			tileX = x; tileY = y;
			do{
				x+=s.smallStepX; y+=s.smallStepY;
				cond = isNotEnd(x, s.endX) && isNotEnd(y, s.endY);
				if(cond && gameState.getField()[x][y] > 0){
					//** Combination situation **//
					if(gameState.getField()[tileX][tileY] == gameState.getField()[x][y] && gameState.getField()[x][y] < 64) {
						gameState.getField()[tileX][tileY]++; //Combine tile
						tileX+=s.smallStepX; tileY+=s.smallStepY;
						gameState.setScore(gameState.getScore() + gameState.getField()[x][y]); //Add to score
						gameState.setTile(x, y, (byte) 0); //Reset old tile
						moved = true;
					}
					//** Move or stay situation **//
					else {
						while((tileX != x || tileY != y) && gameState.getField()[tileX][tileY] != 0){
							tileX+=s.smallStepX; tileY+=s.smallStepY;
						}
						//** Move situation **//
						if(gameState.getField()[tileX][tileY] == 0){
							gameState.getField()[tileX][tileY] = gameState.getField()[x][y]; //Move
							gameState.setTile(x, y, (byte) 0); //Reset old tile
							moved = true;
						}
					}
				}
			}while(cond);
		}
		//Add new tile
		if(moved){
			addRandomTile();
		}
	}

}
