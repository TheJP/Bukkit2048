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
import ch.thejp.plugin.game2048.storage.IUndoable;
import ch.thejp.plugin.game2048.storage.PersistenceUndoable;

/**
 * View-code of the 2048 Plugin 
 * @author JP
 */
public class JP2048Plugin extends JavaPlugin implements Listener {

	//Map Playername->Game
	private Map<String, PlayerGame> games = new HashMap<String, PlayerGame>();
	private IPersistencer persistencer;
	private JPConfiguration config = null;
	private final String configFilename = "plugins/JP2048.yml";
	//Highscores
	private HighscoreManager highscores;
	private boolean readHighscoresSuccess = false;
	private long announced = 0; //Stores, which scores were announced as highscore (antispam)

	/**
	 * Save given game state
	 */
	private void save(IGameState gameState, String itemName, boolean backup){
		highscores.set(itemName, gameState.getScore()); //Sets the highscore if better then the old one
		try { persistencer.write(gameState, itemName, backup); }
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
			receiver.sendMessage(ChatColor.GREEN + String.format("%s (%s, %s, %s)", config.getPhrase("hs"), config.getPhrase("hs-rank"), config.getPhrase("hs-score"), config.getPhrase("hs-name")));
			format = "%d. %d %s";
		}else{
			receiver.sendMessage(ChatColor.GREEN + String.format("%4s %11s %s", config.getPhrase("hs-rank"), config.getPhrase("hs-score"), config.getPhrase("hs-name")));
			format = "%4d. %10d %s";
		}
		int rank = 0, lastRank = 1; long lastScore = Long.MAX_VALUE;
		for(Entry<String, Long> entry : highscores.getSorted()){
			++rank;
			if(entry.getValue() < lastScore){ lastRank = rank; lastScore = entry.getValue(); }
			receiver.sendMessage(String.format(format, lastRank, lastScore, entry.getKey()));
			//Limit highscores
			if(rank >= config.getStatsMaxCount()){ break; }
		}
	}

	/**
	 * Sends a message to the player, if the game is over
	 */
	private void checkGameOver(IGameState gameState, HumanEntity player){
		if(gameState.isGameOver() && player instanceof Player){
			((Player) player).sendMessage(ChatColor.RED + config.getPhrase("game-over"));
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
						config.getPhrase("new-highscore").replace("<player>", player.getName()));
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
		if(config.checkPermission(sender, permission)){ return true; }
		else{
			sender.sendMessage(ChatColor.RED + config.getPhrase("permission-message").replace("<permission>", permission.getName()));
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
				gameLogic = new GameLogic(gameState, false, config.getGameMode());
				checkGameOver(gameState, player);
			}else{
				//No: Start new game
				gameLogic = new GameLogic(gameState, true, config.getGameMode());
				save(gameState, player.getName(), false);
			}
			//Create Inventory for the display
			Inventory inventory = getServer().createInventory(player, InventoryDisplay.COLS*InventoryDisplay.ROWS, config.getPhrase("game-title"));
			//Adapter, which allows the inventory view to undo turns
			IUndoable undoable = new PersistenceUndoable(gameState, persistencer, player.getName(), getLogger(), player.hasPermission(config.getPermissionUnlimitedUndo()));
			//Create the display and render it (=show it to the player)
			InventoryDisplay display = new InventoryDisplay(inventory, gameState, config, undoable);
			display.render();
			InventoryView inventoryView = player.openInventory(inventory); //Open Display
			games.put(player.getName(), new PlayerGame(inventoryView, gameLogic, display)); //Save PlayerGame in RAM
		}
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
		//Load config
		config = new JPConfiguration(YamlConfiguration.loadConfiguration(new File(configFilename)));
		//Create Persistencer
		persistencer = new FilePersistencer(config);
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
		//Is the paly command entered?
		if(command.getName().equals(config.getCommandPlay())){
			//** print stats command **//
			if(args.length > 0 && args[0].equals(config.getCommandStats())){
				//Check for the stats permission
				if(checkPermission(sender, config.getPermissionStats())){
					printHighscores(sender);
					//Execute callback
					if(config.getCallbackStats() != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), config.getCallbackStats());
					}
				}
			}
			//Yes: Is the sender a player?
			else if(!(sender instanceof Player)){
				sender.sendMessage(config.getPhrase("cant-play-on-console"));
			} else {
				//Yes: Show 2048 game board
				Player player = (Player)sender;
				if(args.length > 0 && args[0].equals(config.getCommandNewGame())){
					//Check for "new" permission
					if(!checkPermission(sender, config.getPermissionNew())) { return true; }
					//** Start new game **//
					games.remove(player.getName());
					try { persistencer.delete(player.getName()); }
					catch (IOException e) { getLogger().log(Level.WARNING, "Could not delete game save file", e); return true; }
					//Execute callback
					if(config.getCallbackNewGame() != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), config.getCallbackNewGame());
					}
				} else {
					//Check for "play" permission
					if(!checkPermission(sender, config.getPermissionPlay())) { return true; }
					//** Play existing game **//
					//Execute callback
					if(config.getCallbackPlay() != null){
						getServer().dispatchCommand(getServer().getConsoleSender(), config.getCallbackPlay());
					}
				}
				play(player);
			}
		}
		return true;
	}

	@EventHandler
	public void onInventory(InventoryClickEvent event){
		if(event.getInventory().getName().equals(config.getPhrase("game-title"))){
			event.setCancelled(true);
			//Perform click if possible
			HumanEntity player = event.getWhoClicked();
			if(games.containsKey(player.getName())){
				PlayerGame game = games.get(player.getName());
				//Perform turn
				boolean undoable = game.getDisplay().performClick(game.getGameLogic(), event.getRawSlot());
				//Save game state
				save(game.getGameLogic().getGameState(), player.getName(), undoable);
				//Render game state
				game.getDisplay().render();
				//Game over?
				checkGameOver(game.getGameLogic().getGameState(), player);
			}
		}
	}
}
