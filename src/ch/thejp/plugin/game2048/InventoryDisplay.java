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
	private GameMode gameMode;
	private IConfiguration config;

	//Materials with default values
	private ItemStack arrowUp = new ItemStack(Material.FLINT);
	private ItemStack arrowRight = new ItemStack(Material.ARROW);
	private ItemStack arrowDown = new ItemStack(Material.HOPPER);
	private ItemStack arrowLeft = new ItemStack(Material.CARROT_ITEM);
	private ItemStack filler = new ItemStack(Material.STAINED_GLASS, 1, (short) 15);
	private ItemStack emptyField = new ItemStack(Material.AIR);
	private Material scoreMaterial = Material.STICK;
	private Material zeroMaterial = Material.EGG;

	public InventoryDisplay(Inventory inventory, IGameState gameState, GameMode mode, IConfiguration config) {
		this.inventory = inventory;
		this.gameState = gameState;
		this.contents = new ItemStack[COLS*ROWS];
		this.gameMode = mode;
		this.config = config;
		initContents();
	}
	
	/**
	 * Initialize frame of the display
	 */
	private void initContents(){
		//** Read Materials from config **//
//		Material fillerMaterial = Material.matchMaterial(config.getJPConfig().getString("misc.border-material", "STICK"));
//		short fillerMeta = (short) config.getJPConfig().getInt("misc.border-metadata", 0);
//		if(fillerMaterial != null){ filler = new ItemStack(fillerMaterial, 1, (short) fillerMeta); }
//		else { filler = new ItemStack(Material.STICK); }
		filler = getConfigMaterial("display.border", filler, 1);
		arrowUp = getConfigMaterial("display.arrow.up", arrowUp, 1);
		arrowRight = getConfigMaterial("display.arrow.right", arrowRight, 1);
		arrowDown = getConfigMaterial("display.arrow.down", arrowDown, 1);
		arrowLeft = getConfigMaterial("display.arrow.left", arrowLeft, 1);
		//** Add tooltips **//
		changeDisplayName(arrowUp, ChatColor.GREEN + config.getPhrase("display-up"));
		changeDisplayName(arrowRight, ChatColor.GREEN + config.getPhrase("display-right"));
		changeDisplayName(arrowDown, ChatColor.GREEN + config.getPhrase("display-down"));
		changeDisplayName(arrowLeft, ChatColor.GREEN + config.getPhrase("display-left"));
		changeDisplayName(filler, " ");
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
	 * Reads a Material as ItemStack from the configuration
	 * @param configKey
	 * @param def Default value
	 * @param amount Amount of items in the ItemStack
	 * @return
	 */
	private ItemStack getConfigMaterial(String configKey, ItemStack def, int amount){
		//Read material name
		String materialName = config.getJPConfig().getString(configKey + "-material", null);
		if(materialName == null){ return def; }
		//Match material
		Material match = Material.matchMaterial(materialName);
		if(match == null){ return def; }
		//Set metadata
		short metadata = (short) config.getJPConfig().getInt(configKey + "-metadata", 0);
		return new ItemStack(match, amount, metadata);
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
			if(digit == 0){
				result.add(getConfigMaterial("display.zero", new ItemStack(zeroMaterial), 1));
			} else {
				result.add(getConfigMaterial("display.score", new ItemStack(scoreMaterial, digit), digit));
			}
			score /= 10;
		}
		return result;
	}

	/**
	 * Get default color of tiel in gamemode 2048
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
		if(gameMode == GameMode.GM64){
			if(value > 63){ value = 63; }
			//Default value
			s = new ItemStack(Material.WOOL, (byte) value, (short) (value / 4));
		} else {
			//Default value
			s = new ItemStack(Material.WOOL, 1, calculateColor(value));
		}
		//Get Material from config (or set default)
		s = getConfigMaterial("display.tile." + value, s, s.getAmount());
		//Does not work (will using this as soon as it works):
		//s.setData(new Wool(DyeColor.values()[value / 4]));
		changeDisplayName(s, ChatColor.DARK_GREEN + Long.toString(value));
		return s;
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
		String stringScore = String.format("%s%s: %d", ChatColor.GOLD, config.getPhrase("hs-score"), gameState.getScore());
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
