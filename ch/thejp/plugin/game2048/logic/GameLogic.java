package ch.thejp.plugin.game2048.logic;

public class GameLogic implements IGameLogic {

	/**
	 * Class which represents a moving strategy to keep the moving algorithm more generic
	 * @author JP
	 */
	private static class Strategy {
		public int initX, initY, smallStepX, smallStepY;
		public Strategy(int initX, int initY, int smallStepX, int smallStepY) {
			super();
			this.initX = initX;
			this.initY = initY;
			this.smallStepX = smallStepX;
			this.smallStepY = smallStepY;
		}
	}

	private IGameState gameState;
	private Strategy[] strategies = {
		new Strategy(0, 0, 0, 1), //UP
		new Strategy(GameState.FIELD_SIZE - 1, 0, -1, 0), //RIGHT
		new Strategy(0, GameState.FIELD_SIZE - 1, 0, -1), //DOWN
		new Strategy(0, 0, 1, 0)
	};
	
	/**
	 * Constructor needs valid gamestate
	 * @param gameState
	 */
	public GameLogic(IGameState gameState) {
		this.gameState = gameState;
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

	@Override
	public void move(Direction dir) {
		//The main moving and scoring algorithm is defined here
		Strategy strategy = getMovingStrategy(dir);
	}

}
