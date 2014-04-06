package ch.thejp.plugin.game2048.console;

import java.io.IOException;

import ch.thejp.plugin.game2048.logic.Direction;
import ch.thejp.plugin.game2048.logic.GameLogic;
import ch.thejp.plugin.game2048.logic.GameState;

public class MainConsole {

	public static void main(String[] args) {
		GameState gameState = new GameState();
		GameLogic gameLogic = new GameLogic(gameState);
		char cmd = 'a';
		boolean show = true;
		do{
			try {
				if(show){
					for(int y = 0; y < GameState.FIELD_SIZE; ++y){
						for(int x = 0; x < GameState.FIELD_SIZE; ++x){
							System.out.print("+--");
						}
						System.out.println("+");
						for(int x = 0; x < GameState.FIELD_SIZE; ++x){
							System.out.print("|" + (gameState.getField()[x][y] < 10 ? " " : "") + gameState.getField()[x][y]);
						}
						System.out.println("|");
					}
					for(int x = 0; x < GameState.FIELD_SIZE; ++x){
						System.out.print("+--");
					}
					System.out.println("+");
				}
				//Input
				show = true;
				cmd = (char)System.in.read();
				switch (cmd) {
					case 'w': gameLogic.move(Direction.Up); break;
					case 'd': gameLogic.move(Direction.Right); break;
					case 's': gameLogic.move(Direction.Down); break;
					case 'a': gameLogic.move(Direction.Left); break;
					default: show = false; break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}while(cmd != 'q' && !gameState.isGameOver());
		System.out.println("Game Over");
	}

}
