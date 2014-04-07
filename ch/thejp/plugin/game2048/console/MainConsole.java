package ch.thejp.plugin.game2048.console;

import java.io.IOException;

import ch.thejp.plugin.game2048.logic.Direction;
import ch.thejp.plugin.game2048.logic.GameLogic;
import ch.thejp.plugin.game2048.logic.GameState;
import ch.thejp.plugin.game2048.logic.IGameState;

public class MainConsole {

	private static void printRow(){
		for(int x = 0; x < IGameState.FIELD_SIZE; ++x){
			System.out.print("+--");
		}
		System.out.println("+");
	}
	
	private static void printField(IGameState gameState){
		for(int y = 0; y < GameState.FIELD_SIZE; ++y){
			printRow();
			for(int x = 0; x < GameState.FIELD_SIZE; ++x){
				System.out.print("|" + (gameState.getTile(x, y) < 10 ? " " : "") + gameState.getTile(x, y));
			}
			System.out.println("|");
		}
		printRow();
	}
	
	public static void main(String[] args) {
		GameState gameState = new GameState();
		GameLogic gameLogic = new GameLogic(gameState);
		char cmd = 'a';
		boolean show = true;
		do{
			try {
				if(show){
					printField(gameState);
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
		printField(gameState);
		System.out.println("Game Over");
	}

}
