package ch.thejp.plugin.game2048;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

/**
 * View-code of the 2048 Plugin 
 * @author JP
 */
public class JP2048Plugin extends JavaPlugin implements Listener {

	private Permission permissionPlay = new Permission("thejp.2048.play");
	//Map Playername->Game
	private Map<String, PlayerGame> games = new HashMap<String, PlayerGame>();

	//** Configs **//
	private final String configFilename = "plugins/2048.yml";
	private Configuration config = null;
	private String langSection = "";
	private String commandPlay = "";

	/**
	 * Gets the localized phrase
	 */
	private String getPhrase(String phrase){
		return config.getString(langSection + phrase, phrase);
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onLoad() {
		//Load Configuration
		config = YamlConfiguration.loadConfiguration(new File(configFilename));
		langSection = "lang." + config.getString("lang.lang", "enUs") + ".";
		commandPlay = config.getString("cmd.play", "2048");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Is the paly command entered and does the sender have permission?
		if(command.getName().equals(commandPlay) && sender.hasPermission(permissionPlay)){
			//Yes: Is the sender a player?
			if(!(sender instanceof Player)){
				sender.sendMessage(getPhrase("cant-play-on-console"));
			} else {
				//Yes: Show 2048 game board
				Player player = (Player)sender;
				if(games.containsKey(player.getName())){
					//Game exists already
					PlayerGame game = games.get(player.getName());
					game.getInventoryView().close(); //Make sure only one inventory is open
					//Reopen existing inventory
					game.getDisplay().render();
					game.setInventoryView(player.openInventory(game.getDisplay().getInventory()));
				}else{
					IGameState gameState = new GameState();
					IGameLogic gameLogic = new GameLogic(gameState);
					Inventory inventory = getServer().createInventory(player, 9*6, getPhrase("game-title"));
					InventoryDisplay display = new InventoryDisplay(inventory, gameState);
					display.render();
					InventoryView inventoryView = player.openInventory(inventory);
					games.put(player.getName(), new PlayerGame(inventoryView, gameLogic, display));
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
				//TODO: Save state
			}
		}
	}
}
