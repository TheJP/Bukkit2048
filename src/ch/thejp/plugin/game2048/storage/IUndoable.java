package ch.thejp.plugin.game2048.storage;

import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Interface to undo a turn or to check if an undo is possible
 * @author JP
 *
 */
public interface IUndoable {
	/**
	 * Checks, if the turn can be undone
	 * @return true=yes, false=no
	 */
	boolean isUndoable();
	/**
	 * Undoes the last turn
	 * @return Returns the new (=undone) game state
	 */
	IGameState undo();
}
