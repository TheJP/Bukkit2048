package ch.thejp.plugin.game2048.storage;

import java.io.IOException;

import ch.thejp.plugin.game2048.HighscoreManager;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Interface to save/load a game state and highscore from/to a persistent source (e.g. file)
 * @author JP
 */
public interface IPersistencer {
	/**
	 * Write the game state.
	 * (where and how the state is saved is left to the concrete implementation)
	 * @param gameState State which has to be saved
	 * @param itemName Used as item identifier
	 * @throws IOException 
	 */
	void write(IGameState gameState, String itemName) throws IOException;
	/**
	 * Write the game state.
	 * (where and how the state is saved is left to the concrete implementation)
	 * @param gameState State which has to be saved
	 * @param itemName Used as item identifier
	 * @param backup (default: false) If a backup of the old version should be created
	 * @throws IOException 
	 */
	void write(IGameState gameState, String itemName, boolean backup) throws IOException;
	/**
	 * Read the game state.
	 * (from where and how the state is loaded is left to the concrete implementation)
	 * @param gameState State in which the data has to be loaded
	 * @param itemName Used as item identifier (Same id should give same results)
	 * @throws IOException 
	 */
	void read(IGameState gameState, String itemName) throws IOException;
	/**
	 * Replaces the current save with its backup to undo the last write action.
	 * @throws IOException
	 */
	void undo(String itemName) throws IOException;
	/**
	 * Checks if the Item with given identifier is available.
	 * @param itemName Name of the searched item
	 * @return true=available, false otherwise
	 * @throws IOException
	 */
	boolean isAvailable(String itemName);
	/**
	 * Checks if the Item with given identifier is available.
	 * @param itemName Name of the searched item
	 * @param backup (default: false) true=check if the backup item is available
	 * @return true=available, false otherwise
	 * @throws IOException
	 */
	boolean isAvailable(String itemName, boolean backup);
	/**
	 * Deletes the game state.
	 * @param itemName Identifier of the state
	 */
	void delete(String itemName) throws IOException;
	/**
	 * Reads the given highscores from the persistent storage to the given HighscoreManager.
	 * @param highscores
	 * @throws IOException
	 */
	void readHighscores(HighscoreManager highscores) throws IOException;
	/**
	 * Stores the given highscores persistent.
	 * @param highscores
	 * @throws IOException
	 */
	void writeHighscores(HighscoreManager highscores) throws IOException;
}
