package ch.thejp.plugin.game2048;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ch.thejp.plugin.game2048.logic.Direction;
import ch.thejp.plugin.game2048.logic.GameMode;
import ch.thejp.plugin.game2048.logic.IGameLogic;
import ch.thejp.plugin.game2048.logic.IGameState;
import ch.thejp.plugin.game2048.storage.IUndoable;

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
	private JPConfiguration config;
	private IUndoable undoable;

	private ItemStack emptyField = new ItemStack(Material.AIR);

	public InventoryDisplay(Inventory inventory, IGameState gameState, JPConfiguration config, IUndoable undoable) {
		this.inventory = inventory;
		this.gameState = gameState;
		this.contents = new ItemStack[COLS*ROWS];
		this.config = config;
		this.undoable = undoable;
	}
	
	/**
	 * Initialize frame of the display
	 */
	private void initContents(){
		//** Get Config materials **//
		ItemStack filler = config.getDisplayBorder();
		ItemStack arrowUp = config.getDisplayArrowUp();
		ItemStack arrowRight = config.getDisplayArrowRight();
		ItemStack arrowDown = config.getDisplayArrowDown();
		ItemStack arrowLeft = config.getDisplayArrowLeft();
		ItemStack undo = config.getDisplayUndo();
		//** Add tooltips **//
		changeDisplayName(arrowUp, ChatColor.GREEN + config.getPhrase("display-up"));
		changeDisplayName(arrowRight, ChatColor.GREEN + config.getPhrase("display-right"));
		changeDisplayName(arrowDown, ChatColor.GREEN + config.getPhrase("display-down"));
		changeDisplayName(arrowLeft, ChatColor.GREEN + config.getPhrase("display-left"));
		changeDisplayName(filler, " ");
		changeDisplayName(undo, ChatColor.RED + config.getPhrase("display-undo"));
		//** Create playfield border **//
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
//			if(digit == 0){
//				result.add(getConfigMaterial("display.zero", new ItemStack(zeroMaterial), 1));
//			} else {
//				result.add(getConfigMaterial("display.score", new ItemStack(scoreMaterial, digit), digit));
//			}
			result.add(config.getDisplayScoreDigit(digit));
			score /= 10;
		}
		return result;
	}

	/**
	 * Get default color of tile in gamemode 2048
	 * @param value
	 * @return
	 */
	private short calculateColor(long value){
	    return (short) ((Math.log(value) / Math.log(2)) % 16);
	}

	/**
	 * Returns the ItemStack of the field
	 * @param value
	 * @return
	 */
	private ItemStack getTileItems(long value){
		if(value < 0){ value = 0; }
		ItemStack s;
		if(config.getGameMode() == GameMode.GM64){
			if(value > 63){ value = 63; }
			//Default value
			s = new ItemStack(Material.WOOL, (byte) value, (short) (value / 4));
		} else {
			//Default value
			s = new ItemStack(Material.WOOL, 1, calculateColor(value));
		}
		//Get Material from config (or set default)
		s = config.getDisplayTile(value, s);
		//Does not work (will using this as soon as it works):
		//s.setData(new Wool(DyeColor.values()[value / 4]));
		changeDisplayName(s, ChatColor.DARK_GREEN +
				config.getJPConfig().getString("display.tile-color", "") + Long.toString(value));
		return s;
	}

	/**
	 * Create the inventory content, which shows the game board
	 */
	public void render(){
		initContents();
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
		//undo button
		contents[COLS-2] = (undoable.isUndoable() ? config.getDisplayUndo() : emptyField);
		//score display
		List<ItemStack> score = scoreToDisplay();
		String stringScore = String.format("%s%s: %d", ChatColor.GOLD, config.getPhrase("display-score"), gameState.getScore());
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
	 * @return If this is a turn, which could be "undoable"
	 */
	public boolean performClick(IGameLogic gameLogic, int slot){
		Direction dir = null;
		switch (slot) {
		case 2: case 3:
			dir = Direction.Up; break;
		case 2*COLS: case 3*COLS:
			dir = Direction.Left; break;
		case 2*COLS+5: case 3*COLS+5:
			dir = Direction.Right; break;
		case 5*COLS+2: case 5*COLS+3:
			dir = Direction.Down; break;
		case COLS-2:
			undoable.undo(); break;
		default: break;
		}
		if(dir != null){ return gameLogic.move(dir); }
		else { return false; }
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
