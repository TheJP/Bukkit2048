package ch.thejp.plugin.game2048;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ch.thejp.plugin.game2048.logic.Direction;
import ch.thejp.plugin.game2048.logic.IGameLogic;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Displays a GameState inside of a minecraft Inventory
 * @author JP
 */
public class InventoryDisplay {

	public static final int COLS = 9;
	public static final int ROWS = 6;

	private Inventory inventory;
	private IGameState gameState;
	private ItemStack[] contents;
	private IPhraser phraser;

	//TODO: Add tooltip to the different arrows
	private ItemStack arrowUp = new ItemStack(Material.FLINT);
	private ItemStack arrowDown = new ItemStack(Material.HOPPER);
	private ItemStack arrowLeft = new ItemStack(Material.CARROT_ITEM);
	private ItemStack arrowRight = new ItemStack(Material.ARROW);
	private ItemStack filler = new ItemStack(Material.STICK);
	private ItemStack emptyField = new ItemStack(Material.AIR);
	private Material scoreMaterial = Material.STICK;
	private Material zeroMaterial = Material.EGG;

	public InventoryDisplay(Inventory inventory, IGameState gameState, IPhraser phraser) {
		this.inventory = inventory;
		this.gameState = gameState;
		this.contents = new ItemStack[COLS*ROWS];
		this.phraser = phraser;
		initContents();
	}
	
	/**
	 * Initialize frame of the display
	 */
	private void initContents(){
		changeDisplayName(arrowUp, ChatColor.GREEN + phraser.getPhrase("display-up"));
		changeDisplayName(arrowRight, ChatColor.GREEN + phraser.getPhrase("display-right"));
		changeDisplayName(arrowDown, ChatColor.GREEN + phraser.getPhrase("display-down"));
		changeDisplayName(arrowLeft, ChatColor.GREEN + phraser.getPhrase("display-left"));
		changeDisplayName(filler, " ");
		contents[0] = contents[1] = contents[4] = contents[5] = filler;
		contents[2] = contents[3] = arrowUp;
		int i = COLS;
		contents[i] = contents[i+5] = filler; i+=COLS;
		contents[i] = contents[i+COLS] = arrowLeft;
		contents[i+5] = contents[i+COLS+5] = arrowRight; i+=COLS+COLS;
		contents[i] = contents[i+5] = filler; i+=COLS;
		contents[i] = contents[i+1] = contents[i+4] = contents[i+5] = filler;
		contents[i+2] = contents[i+3] = arrowDown;
	}

	/**
	 * Method to set the displayname of an ItemStack
	 * @param s ItemStack
	 * @param name New display name (ChatColor can be used)
	 */
	private void changeDisplayName(ItemStack s, String name){
		ItemMeta m = s.getItemMeta();
		m.setDisplayName(name);
		s.setItemMeta(m);
	}

	/**
	 * Callculate the score display
	 * @return score display
	 */
	private List<ItemStack> scoreToDisplay(){
		List<ItemStack> result = new ArrayList<ItemStack>(ROWS);
		long score = gameState.getScore();
		while(score > 0){
			int digit = (int)(score % 10);
			if(digit == 0){ result.add(new ItemStack(zeroMaterial)); }
			else{ result.add(new ItemStack(scoreMaterial, digit)); }
			score /= 10;
		}
		return result;
	}

	/**
	 * Returns the ItemStack of the field
	 * @param value Valid data: 0-63
	 * @return
	 */
	private ItemStack getTileItems(long value){
		if(value > 63){ value = 63; }
		else if(value < 0){ value = 0; }
//		ItemStack s = new ItemStack(Material.WOOL, value, (short) (value / 4));
		//Does not work (will using this as soon as it works):
		//s.setData(new Wool(DyeColor.values()[value / 4]));
//		changeDisplayName(s, ChatColor.DARK_GREEN + Byte.toString(value));
		return null;
	}

	/**
	 * Create the inventory content, which shows the game board
	 */
	public void render(){
		//4*4 board
		for(int y = 0; y < IGameState.FIELD_SIZE; ++y){
			int r = (COLS*(y+1)) + 1;
			for(int x = 0; x < IGameState.FIELD_SIZE; ++x){
				if(gameState.getTile(x, y) > 0){
					contents[x + r] = getTileItems(gameState.getTile(x, y));
				}else{
					contents[x + r] = emptyField;
				}
			}
		}
		//score display
		List<ItemStack> score = scoreToDisplay();
		String stringScore = String.format("%s%s: %d", ChatColor.GOLD, phraser.getPhrase("hs-score"), gameState.getScore());
		int row = (Math.min(score.size(), ROWS) * COLS) - 1;
		for(ItemStack item : score){
			changeDisplayName(item, stringScore);
			contents[row] = item;
			row -= COLS;
		}
		inventory.setContents(contents);
	}

	/**
	 * Perform a click event on the board
	 * @param gameLogic Object to perform the click on it
	 * @param slot Slot, in which the player clicked
	 */
	public void performClick(IGameLogic gameLogic, int slot){
		switch (slot) {
		case 2: case 3:
			gameLogic.move(Direction.Up); break;
		case 2*COLS: case 3*COLS:
			gameLogic.move(Direction.Left); break;
		case 2*COLS+5: case 3*COLS+5:
			gameLogic.move(Direction.Right); break;
		case 5*COLS+2: case 5*COLS+3:
			gameLogic.move(Direction.Down); break;
		default: break;
		}
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
