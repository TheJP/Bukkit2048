package ch.thejp.plugin.game2048.storage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import ch.thejp.plugin.game2048.JPConfiguration;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Adapbter class to IPersistencer, which implements IUndoable
 * @author JP
 *
 */
public class PersistenceUndoable implements IUndoable {

	private IGameState gameState;
	private IPersistencer persistencer;
	private Player player;
	private Logger logger;
	private JPConfiguration config;

	/**
	 * @param gameState Game state in which the undo should be procesed
	 * @param persistencer IPersistencer to adapt
	 * @param player Player, to which this IUndoable belongs to
	 * @param logger Logger, to log IOExceptions into
	 * @param unlimited true=the player has unlimited undo operations, false otherwise
	 */
	public PersistenceUndoable(IGameState gameState, IPersistencer persistencer, Player player, Logger logger, JPConfiguration config) {
		this.gameState = gameState;
		this.persistencer = persistencer;
		this.player = player;
		this.logger = logger;
		this.config = config;
	}

	protected boolean isPermitted() {
		return config.checkPermission(player, config.getPermissionUnlimitedUndo());
	}

	@Override
	public boolean isUndoable() {
		return isPermitted() && persistencer.isAvailable(player.getName(), true);
	}

	@Override
	public void undo() {
		try {
			//Load backup file
			persistencer.undo(player.getName(), isPermitted());
			//Read backup file
			persistencer.read(gameState, player.getName());
		}
		catch (IOException e) { logger.log(Level.WARNING, "Could not undo turn", e); }
	}
}
