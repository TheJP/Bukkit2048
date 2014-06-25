package ch.thejp.plugin.game2048;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.permissions.Permission;

import ch.thejp.plugin.game2048.logic.GameMode;

public class JPConfiguration implements IConfiguration {

	private Configuration config = null;

	public JPConfiguration(Configuration config){
		this.config = config;
	}

	/**
	 * Section, which is used for phrases / translations.
	 * @return
	 */
	protected String getLangSection() {
		return "lang." + config.getString("lang.lang", "enUs") + ".";
	}

	/**
	 * Loads / Reloads the configuration.
	 */
	public void reload(Configuration config){
		this.config = config;
	}

	@Override
	public String getPhrase(String phrase){
		return config.getString(getLangSection() + phrase, phrase);
	}

	@Override
	public Configuration getJPConfig() {
		return config;
	}

	@Override
	public GameMode getGameMode() {
		return config.getString("misc.game-mode", "2048").equals("64") ? GameMode.GM64 : GameMode.GM2048;
	}

	/**
	 * Checks if the given sender has the given permission.
	 * @param sender Sender for which the permission will be checked
	 * @param permission Permission, which will be checked
	 * @return True if the sender has the permission, false otherwise
	 */
	public boolean checkPermission(CommandSender sender, Permission permission){
		return !isEnabledPermissions() || sender.hasPermission(permission);
	}

	public boolean isEnabledPermissions() {
		return config.getBoolean("misc.perm", false);
	}

	public Permission getPermissionPlay() {
		return new Permission(config.getString("perm.play", "thejp.2048.play"));
	}

	public Permission getPermissionNew() {
		return new Permission(config.getString("perm.new", "thejp.2048.new"));
	}

	public Permission getPermissionStats() {
		return new Permission(config.getString("perm.stats", "thejp.2048.stats"));
	}

	public Permission getPermissionUnlimitedUndo() {
		return new Permission(config.getString("perm.unlimited-undo", "thejp.2048.undo.unlimited"));
	}

	public String getCommandPlay() {
		return config.getString("cmd.play", "2048");
	}

	public String getCommandNewGame() {
		return config.getString("cmd.new", "new");
	}

	public String getCommandStats() {
		return config.getString("cmd.stats", "stats");
	}

	public String getCallbackPlay() {
		return config.getString("callback.cmd.play", null);
	}

	public String getCallbackNewGame() {
		return config.getString("callback.cmd.new", null);
	}

	public String getCallbackStats() {
		return config.getString("callback.cmd.stats", null);
	}

	public int getStatsMaxCount() {
		return config.getInt("misc.stats-max-count", 10);
	}

	public String getStoragePath() {
		return config.getString("storage.path", "plugins/JP2048/");
	}
}
