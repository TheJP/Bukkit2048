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
	private boolean unlimited;

	/**
	 * @param gameState Game state in which the undo should be procesed
	 * @param persistencer IPersistencer to adapt
	 * @param itemName Item, to which this adapter belongs
	 * @param logger Logger, to log IOExceptions into
	 * @param unlimited true=the player has unlimited undo operations, false otherwise
	 */
	public PersistenceUndoable(IGameState gameState, IPersistencer persistencer, String itemName, Logger logger, boolean unlimited) {
		this.gameState = gameState;
		this.persistencer = persistencer;
		this.itemName = itemName;
		this.logger = logger;
		this.unlimited = unlimited;
	}

	@Override
	public boolean isUndoable() {
		return unlimited && persistencer.isAvailable(itemName, true);
	}

	@Override
	public void undo() {
		try {
			//Load backup file
			persistencer.undo(itemName, unlimited);
			//Read backup file
			persistencer.read(gameState, itemName);
		}
		catch (IOException e) { logger.log(Level.WARNING, "Could not undo turn", e); }
	}
}
