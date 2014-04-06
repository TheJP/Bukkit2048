package ch.thejp.plugin.game2048.logic;

public class GameLogic implements IGameLogic {

	private IGameState gameState;
	
	
	/**
	 * Constructor needs valid gamestate
	 * @param gameState
	 */
	public GameLogic(IGameState gameState){
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
	 * Gets an int version of the Direction
	 * @param dir
	 * @return
	 */
	private int getMovingStrategy(Direction dir){
		switch(dir){
		case Up:    return 0;
		case Right: return 1;
		case Down:  return 2;
		case Left:  return 3;
		default:    return 0;
		}
	}

	@Override
	public void move(Direction dir) {
		//The main moving and scoring algorithm is defined here
		int strategy = getMovingStrategy(dir);
	}

}
