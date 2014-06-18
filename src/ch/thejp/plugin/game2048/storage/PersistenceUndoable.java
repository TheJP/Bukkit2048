package ch.thejp.plugin.game2048.storage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Adapbter class to IPersistencer, which implements IUndoable
 * @author JP
 *
 */
public class PersistenceUndoable implements IUndoable {

	private IGameState gameState;
	private IPersistencer persistencer;
	private String itemName;
	private Logger logger;

	/**
	 * @param persistencer IPersistencer to adapt
	 * @param itemName Item, to which this adapter belongs
	 * @param logger Logger, to log IOExceptions into
	 */
	public PersistenceUndoable(IGameState gameState, IPersistencer persistencer, String itemName, Logger logger) {
		this.gameState = gameState;
		this.persistencer = persistencer;
		this.itemName = itemName;
		this.logger = logger;
	}

	@Override
	public boolean isUndoable() {
		return persistencer.isAvailable(itemName, true);
	}

	@Override
	public void undo() {
		try {
			//Load backup file
			persistencer.undo(itemName);
			//Read backup file
			persistencer.read(gameState, itemName);
		}
		catch (IOException e) { logger.log(Level.WARNING, "Could not undo turn", e); }
	}
}
