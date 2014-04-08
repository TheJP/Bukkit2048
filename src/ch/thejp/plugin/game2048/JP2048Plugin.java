package ch.thejp.plugin.game2048;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * View-code of the 2048 Plugin 
 * @author JP
 */
public class JP2048Plugin extends JavaPlugin {

	private Permission permissionPlay = new Permission("thejp.2048.play");

	//** Configs **//
	private final String configFilename = "2048.yml";
	private Configuration config = null;
	private String langSection = "";
	private String commandPlay = "";
	
	@Override
	public void onLoad() {
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
				sender.sendMessage(config.getString(langSection + "CantPlayOnConsole"));
			} else {
				//Yes: Show 2048 game board
			}
		}
		return false;
	}
}
