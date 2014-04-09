package ch.thejp.plugin.game2048;


import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import ch.thejp.plugin.game2048.logic.GameState;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Displays a GameState inside of a minecraft Inventory
 * @author JP
 */
public class InventoryDisplay {

	public static final int ROW = 9;

	private Inventory inventory;
	private IGameState gameState;
	private ItemStack[] contents;

	private ItemStack arrowUp = new ItemStack(Material.FLINT);
	private ItemStack arrowDown = new ItemStack(Material.HOPPER);
	private ItemStack arrowLeft = new ItemStack(Material.CARROT_ITEM);
	private ItemStack arrowRight = new ItemStack(Material.ARROW);
	private ItemStack filler = new ItemStack(Material.STICK);
	private Material fieldMaterial = Material.COBBLESTONE;

	public InventoryDisplay(Inventory inventory, IGameState gameState) {
		this.inventory = inventory;
		this.gameState = gameState;
		this.contents = new ItemStack[ROW*6];
		initContents();
	}
	
	private void initContents(){
		contents[0] = contents[1] = contents[4] = contents[5] = filler;
		contents[2] = contents[3] = arrowUp;
		int i = ROW;
		contents[i] = contents[i+5] = filler; i+=ROW;
		contents[i] = contents[i+ROW] = arrowLeft;
		contents[i+5] = contents[i+ROW+5] = arrowRight; i+=ROW+ROW;
		contents[i] = contents[i+5] = filler; i+=ROW;
		contents[i] = contents[i+1] = contents[i+4] = contents[i+5] = filler;
		contents[i+2] = contents[i+3] = arrowDown;
	}
	
	public void render(){
		for(int y = 0; y < IGameState.FIELD_SIZE; ++y){
			int r = (ROW*(y+1)) + 1;
			for(int x = 0; x < IGameState.FIELD_SIZE; ++x){
				if(gameState.getTile(x, y) > 0){
					contents[x + r] = new ItemStack(fieldMaterial, gameState.getTile(x, y));
				}
			}
		}
		inventory.setContents(contents);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public IGameState getGameState() {
		return gameState;
	}

	public void setGameState(IGameState gameState) {
		this.gameState = gameState;
	}
}
