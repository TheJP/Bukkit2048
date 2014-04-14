package ch.thejp.plugin.game2048;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import ch.thejp.plugin.game2048.logic.GameLogic;
import ch.thejp.plugin.game2048.logic.GameState;
import ch.thejp.plugin.game2048.logic.IGameLogic;
import ch.thejp.plugin.game2048.logic.IGameState;
import ch.thejp.plugin.game2048.storage.FilePersistencer;
import ch.thejp.plugin.game2048.storage.IPersistencer;

/**
 * View-code of the 2048 Plugin 
 * @author JP
 */
public class JP2048Plugin extends JavaPlugin implements Listener {

	private Permission permissionPlay = new Permission("thejp.2048.play");
	//Map Playername->Game
	private Map<String, PlayerGame> games = new HashMap<String, PlayerGame>();
	private IPersistencer persistencer;
	//Highscores
	private HighscoreManager highscores;
	private boolean readHighscoresSuccess = false;

	//** Configs **//
	private final String configFilename = "plugins/JP2048.yml";
	private Configuration config = null;
	private String langSection = "";
	private String commandPlay = "";
	private String commandNewGame = "";
	private String commandStats = "";

	/**
	 * Gets the localized phrase
	 */
	private String getPhrase(String phrase){
		return config.getString(langSection + phrase, phrase);
	}

	/**
	 * Save given game state
	 */
	private void save(IGameState gameState, String itemName){
		highscores.set(itemName, gameState.getScore()); //Sets the highscore if better then the old one
		try { persistencer.write(gameState, itemName); }
		catch (IOException e) { getLogger().log(Level.WARNING, "Could not write game save file", e); }
		if(readHighscoresSuccess){ //Only write highscores if the highscores were read successfully (=> don't overwrite highscores)
			try { persistencer.writeHighscores(highscores); }
			catch (IOException e) { getLogger().log(Level.WARNING, "Could not write highscore save file", e); highscores = null; }
		}else{
			getLogger().log(Level.WARNING, "Did not save highscores, because could not load properly before");
		}
	}

	/**
	 * Print sorted highscores to the player
	 * @param receiver Receiver of the stats command
	 */
	private void printHighscores(CommandSender receiver){
		receiver.sendMessage(ChatColor.GREEN + String.format("%4s %11s %s", "Rank", "Score", "Name"));
		int rank = 0, lastRank = 1; long lastScore = Long.MAX_VALUE;
		for(Entry<String, Long> entry : highscores.getSorted()){
			++rank;
			if(entry.getValue() < lastScore){ lastRank = rank; lastScore = entry.getValue(); }
			receiver.sendMessage(String.format("%4d. %10d %s", lastRank, lastScore, entry.getKey()));
		}
	}

	/**
	 * Sends a message to the player, if the game is over
	 */
	private void checkGameOver(IGameState gameState, HumanEntity player){
		if(gameState.isGameOver() && player instanceof Player){
			((Player) player).sendMessage(ChatColor.RED + getPhrase("game-over"));
		}
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		// TODO Close open InventoryViews
	}
	
	@Override
	public void onLoad() {
		//Load Configuration
		config = YamlConfiguration.loadConfiguration(new File(configFilename));
		langSection = "lang." + config.getString("lang.lang", "enUs") + ".";
		commandPlay = config.getString("cmd.play", "2048");
		commandNewGame = config.getString("cmd.new", "new");
		commandStats = config.getString("cmd.stats", "stats");
		//Create Persistencer
		String storagePath = config.getString("storage.path", "plugins/JP2048/");
		File storage = new File(storagePath);
		storage.mkdirs(); //Create folder structure if it doesn' exist
		persistencer = new FilePersistencer(storage.getAbsolutePath() + File.separatorChar);
		//Load highscores
		highscores = new HighscoreManager();
		try { persistencer.readHighscores(highscores); readHighscoresSuccess = true; }
		catch (IOException e) {
			getLogger().log(Level.WARNING, "Could not read highscore save file", e);
			readHighscoresSuccess = false;
			highscores = new HighscoreManager(); //Empty the highscores (if the data was partially read)
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Is the paly command entered and does the sender have permission?
		if(command.getName().equals(commandPlay) && sender.hasPermission(permissionPlay)){
			//print stats command
			if(args.length > 0 && args[0].equals(commandStats)){
				printHighscores(sender);
			}
			//Yes: Is the sender a player?
			else if(!(sender instanceof Player)){
				sender.sendMessage(getPhrase("cant-play-on-console"));
			} else {
				//Yes: Show 2048 game board
				Player player = (Player)sender;
				if(args.length > 0 && args[0].equals(commandNewGame)){
					//Start new game
					games.remove(player.getName());
					try { persistencer.delete(player.getName()); }
					catch (IOException e) { getLogger().log(Level.WARNING, "Could not delete game save file", e); return true; }
				}
				if(games.containsKey(player.getName())){
					//Game exists already
					PlayerGame game = games.get(player.getName());
					game.getInventoryView().close(); //Make sure only one inventory is open
					//Reopen existing inventory
					game.getDisplay().render();
					game.setInventoryView(player.openInventory(game.getDisplay().getInventory()));
				}else{
					IGameState gameState = new GameState();
					IGameLogic gameLogic;
					//Is a save file available?
					if(persistencer.isAvailable(player.getName())){
						//Yes: Read save file
						try { persistencer.read(gameState, player.getName()); }
						catch (IOException e) { getLogger().log(Level.WARNING, "Could not read game save file", e); return true; }
						gameLogic = new GameLogic(gameState, false);
						checkGameOver(gameState, player);
					}else{
						//No: Start new game
						gameLogic = new GameLogic(gameState);
						save(gameState, player.getName());
					}
					//Create Display
					Inventory inventory = getServer().createInventory(
							player, InventoryDisplay.COLS*InventoryDisplay.ROWS, getPhrase("game-title"));
					InventoryDisplay display = new InventoryDisplay(inventory, gameState);
					display.render();
					InventoryView inventoryView = player.openInventory(inventory); //Open Display
					games.put(player.getName(), new PlayerGame(inventoryView, gameLogic, display)); //Save PlayerGame in RAM
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onInventory(InventoryClickEvent event){
		if(event.getInventory().getName().equals(getPhrase("game-title"))){
			event.setCancelled(true);
			//Perform click if possible
			HumanEntity player = event.getWhoClicked();
			if(games.containsKey(player.getName())){
				PlayerGame game = games.get(player.getName());
				game.getDisplay().performClick(game.getGameLogic(), event.getRawSlot());
				game.getDisplay().render();
				//Save game state
				save(game.getGameLogic().getGameState(), player.getName());
				//Game over?
				checkGameOver(game.getGameLogic().getGameState(), player);
			}
		}
	}
}
