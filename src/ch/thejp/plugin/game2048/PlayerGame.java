package ch.thejp.plugin.game2048;

import org.bukkit.inventory.InventoryView;

import ch.thejp.plugin.game2048.logic.IGameLogic;

/**
 * Class to save a 2048 game in the RAM (while playing)
 * @author JP
 */
public class PlayerGame {

	private InventoryView inventoryView;
	private IGameLogic gameLogic;
	private InventoryDisplay display;

	public PlayerGame(InventoryView inventoryView, IGameLogic gameLogic,
			InventoryDisplay display) {
		this.inventoryView = inventoryView;
		this.gameLogic = gameLogic;
		this.display = display;
	}
	public InventoryView getInventoryView() {
		return inventoryView;
	}
	public void setInventoryView(InventoryView inventoryView) {
		this.inventoryView = inventoryView;
	}
	public IGameLogic getGameLogic() {
		return gameLogic;
	}
	public void setGameLogic(IGameLogic gameLogic) {
		this.gameLogic = gameLogic;
	}
	public InventoryDisplay getDisplay() {
		return display;
	}
	public void setDisplay(InventoryDisplay display) {
		this.display = display;
	}
}

