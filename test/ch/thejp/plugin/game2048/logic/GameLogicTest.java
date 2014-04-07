package ch.thejp.plugin.game2048.logic;

import static org.junit.Assert.*;

import org.junit.Test;

public class GameLogicTest {

	@Test
	public void testGameOver() {
		GameState gameState = new GameState();
		byte k = 1;
		for(int x = 0; x < IGameState.FIELD_SIZE; ++x)
			for(int y = 0; y < IGameState.FIELD_SIZE; ++y)
				gameState.setTile(x, y, k++);
		assertTrue(gameState.isGameOver());
		gameState.setTile(0,1,(byte)1);
		assertFalse(gameState.isGameOver());
		gameState.setTile(0,0,(byte)2);
		assertTrue(gameState.isGameOver());
	}

}
