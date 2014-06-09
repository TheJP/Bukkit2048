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
import ch.thejp.plugin.game2048.logic.GameMode;
import ch.thejp.plugin.game2048.logic.GameState;
import ch.thejp.plugin.game2048.logic.IGameLogic;
import ch.thejp.plugin.game2048.logic.IGameState;
import ch.thejp.plugin.game2048.storage.FilePersistencer;
import ch.thejp.plugin.game2048.storage.IPersistencer;

/**
 * View-code of the 2048 Plugin 
 * @author JP
 */
public class JP2048Plugin extends JavaPlugin implements Listener, IConfiguration {

	//Map Playername->Game
	private Map<String, PlayerGame> games = new HashMap<String, PlayerGame>();
	private IPersistencer persistencer;
	//Highscores
	private HighscoreManager highscores;
	private boolean readHighscoresSuccess = false;
	private long announced = 0; //Stores, which scores were announced as highscore (antispam)

	//** Configs **//
	private boolean enabledPermissions;
	private Permission permissionPlay;
	private Permission permissionNew;
	private Permission permissionStats;
	private final String configFilename = "plugins/JP2048.yml";
	private Configuration config = null;
	private String langSection = "";
	private String commandPlay = "";
	private String commandNewGame = "";
	private String commandStats = "";
	private String callbackPlay = null;
	private String callbackNewGame = null;
	private String callbackStats = null;
	private GameMode gameMode = GameMode.GM64;

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
	 * Print sorted highscores to the player / console
	 * @param receiver Receiver of the stats command
	 */
	private void printHighscores(CommandSender receiver){
		String format;
		if(receiver instanceof Player){
			receiver.sendMessage(ChatColor.GREEN + String.format("%s (%s, %s, %s)", getPhrase("hs"), getPhrase("hs-rank"), getPhrase("hs-score"), getPhrase("hs-name")));
			format = "%d. %d %s";
		}else{
			receiver.sendMessage(ChatColor.GREEN + String.format("%4s %11s %s", getPhrase("hs-rank"), getPhrase("hs-score"), getPhrase("hs-name")));
			format = "%4d. %10d %s";
		}
		int rank = 0, lastRank = 1; long lastScore = Long.MAX_VALUE;
		for(Entry<String, Long> entry : highscores.getSorted()){
			++rank;
			if(entry.getValue() < lastScore){ lastRank = rank; lastScore = entry.getValue(); }
			receiver.sendMessage(String.format(format, lastRank, lastScore, entry.getKey()));
			//Limit highscores
			if(rank >= config.getInt("misc.stats-max-count", 10)){ break; }
		}
	}

	/**
	 * Sends a message to the player, if the game is over
	 */
	private void checkGameOver(IGameState gameState, HumanEntity player){
		if(gameState.isGameOver() && player instanceof Player){
			((Player) player).sendMessage(ChatColor.RED + getPhrase("game-over"));
			//Check for new highscore
			Entry<String, Long>[] hs = highscores.getSorted();
			//Is the current score a new highscore?
			if(hs.length > 0 &&
				hs[0].getKey().equals(player.getName()) &&
				hs[0].getValue() == gameState.getScore() &&
				announced < gameState.getScore()
			){
				//Yes: Announce
				getServer().broadcastMessage(ChatColor.GREEN +
						getPhrase("new-highscore").replace("<player>", player.getName()));
				announced = gameState.getScore();
			}
		}
	}

	/**
	 * Checks if the given sender has the given permission.
	 * If not the sender will receive the configured chat message
	 * @param sender Sender for which the permission will be checked
	 * @param permission Permission, which will be checked
	 * @return True if the sender has the permission, false otherwise
	 */
	private boolean checkPermission(CommandSender sender, Permission permission){
		if(!enabledPermissions || sender.hasPermission(permission)){ return true; }
		else{
			sender.sendMessage(ChatColor.RED + getPhrase("permission-message"));
			return false;
		}
	}

	/**
	 * Starts / continues the game (by opening the game inventory) 
	 * @param player
	 */
	private void play(Player player){
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
				catch (IOException e) { getLogger().log(Level.WARNING, "Could not read game save file", e); return; }
				gameLogic = new GameLogic(gameState, false, gameMode);
				checkGameOver(gameState, player);
			}else{
				//No: Start new game
				gameLogic = new GameLogic(gameState, true, gameMode);
				save(gameState, player.getName());
			}
			//Create Display
			Inventory inventory = getServer().createInventory(
					player, InventoryDisplay.COLS*InventoryDisplay.ROWS, getPhrase("game-title"));
			InventoryDisplay display = new InventoryDisplay(inventory, gameState, gameMode, this);
			display.render();
			InventoryView inventoryView = player.openInventory(inventory); //Open Display
			games.put(player.getName(), new PlayerGame(inventoryView, gameLogic, display)); //Save PlayerGame in RAM
		}
	}

	@Override
	public String getPhrase(String phrase){
		return config.getString(langSection + phrase, phrase);
	}

	@Override
	public Configuration getJPConfig(){
		return config;
	}

	@Override
	public GameMode getGameMode() {
		return gameMode;
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		//Close open InventoryViews
		for(Entry<String, PlayerGame> entry : games.entrySet()){
			entry.getValue().getInventoryView().close();
		}
	}

	@Override
	public void onLoad() {
		//Load Configuration
		config = YamlConfiguration.loadConfiguration(new File(configFilename));
		langSection = "lang." + config.getString("lang.lang", "enUs") + ".";
		commandPlay = config.getString("cmd.play", "2048");
		commandNewGame = config.getString("cmd.new", "new");
		commandStats = config.getString("cmd.stats", "stats");
		callbackPlay = config.getString("callback.cmd.play", null);
		callbackNewGame = config.getString("callback.cmd.new", null);
		callbackStats = config.getString("callback.cmd.stats", null);
		gameMode = config.getString("misc.game-mode", "2048").equals("64") ? GameMode.GM64 : GameMode.GM2048;
		enabledPermissions = config.getBoolean("misc.perm", false);
		permissionPlay = new Permission(config.getString("perm.play", "thejp.2048.play"));
		permissionNew = new Permission(config.getString("perm.new", "thejp.2048.new"));
		permissionStats = new Permission(config.getString("perm.stats", "thejp.2048.stats"));
		//Create Persistencer
		String storagePath = config.getString("storage.path", "plugins/JP2048/");
		File storage = new File(storagePath);
		storage.mkdirs(); //Create folder structure if it doesn' exist
		persistencer = new FilePersistencer(storage.getAbsolutePath() + File.separatorChar, this);
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
			//** print stats command **//
			if(args.length > 0 && args[0].equals(commandStats)){
				//Check for the stats permission
				if(checkPermission(sender, permissionStats)){
					printHighscores(sender);
					//Execute callback
					if(callbackStats != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), callbackStats);
					}
				}
			}
			//Yes: Is the sender a player?
			else if(!(sender instanceof Player)){
				sender.sendMessage(getPhrase("cant-play-on-console"));
			} else {
				//Yes: Show 2048 game board
				Player player = (Player)sender;
				if(args.length > 0 && args[0].equals(commandNewGame)){
					//Check for "new" permission
					if(!checkPermission(sender, permissionNew)) { return true; }
					//** Start new game **//
					games.remove(player.getName());
					try { persistencer.delete(player.getName()); }
					catch (IOException e) { getLogger().log(Level.WARNING, "Could not delete game save file", e); return true; }
					//Execute callback
					if(callbackNewGame != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), callbackNewGame);
					}
				} else {
					//Check for "play" permission
					if(!checkPermission(sender, permissionPlay)) { return true; }
					//** Play existing game **//
					//Execute callback
					if(callbackPlay != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), callbackPlay);
					}
				}
				play(player);
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
