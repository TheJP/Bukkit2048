package ch.thejp.plugin.game2048;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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
	 * Reads a Material as ItemStack from the configuration
	 * @param configKey
	 * @param def Default value
	 * @param amount Amount of items in the ItemStack
	 * @return
	 */
	protected ItemStack getConfigMaterial(String configKey, ItemStack def, int amount){
		//Read material name
		String materialName = config.getString(configKey + "-material", null);
		if(materialName == null){ return def; }
		//Match material
		Material match = Material.matchMaterial(materialName);
		if(match == null){ return def; }
		//Set metadata
		short metadata = (short) config.getInt(configKey + "-metadata", 0);
		return new ItemStack(match, amount, metadata);
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
		//If the default of the permission is "op", the permission is considered a admin / vip permission.
		//This admin / vip permissions are checked even if permissions are turned off
		return (permission.getDefault() != PermissionDefault.OP && !isEnabledPermissions()) || sender.hasPermission(permission);
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

	//** Storage **//

	public String getStoragePath() {
		return config.getString("storage.path", "plugins/JP2048/");
	}

	public String getHighscoreFile() {
		return config.getString("storage.highscore-file", "hs.csv");
	}

	//** Materials **//

	public ItemStack getDisplayBorder(){
		return getConfigMaterial("display.border", new ItemStack(Material.STAINED_GLASS, 1, (short) 15), 1);
	}

	public ItemStack getDisplayArrowUp(){
		return getConfigMaterial("display.arrow.up", new ItemStack(Material.FLINT), 1);
	}

	public ItemStack getDisplayArrowRight(){
		return getConfigMaterial("display.arrow.right", new ItemStack(Material.ARROW), 1);
	}

	public ItemStack getDisplayArrowDown(){
		return getConfigMaterial("display.arrow.down", new ItemStack(Material.HOPPER), 1);
	}

	public ItemStack getDisplayArrowLeft(){
		return getConfigMaterial("display.arrow.left", new ItemStack(Material.CARROT_ITEM), 1);
	}

	public ItemStack getDisplayUndo(){
		return getConfigMaterial("display.undo", new ItemStack(Material.FLOWER_POT_ITEM), 1);
	}

	public ItemStack getDisplayScoreDigit(int digit){
		if(digit == 0){
			return getConfigMaterial("display.zero", new ItemStack(Material.EGG), 1);
		} else {
			return getConfigMaterial("display.score", new ItemStack(Material.STICK, digit), digit);
		}
	}

	public ItemStack getDisplayTile(long value, ItemStack def) {
		return getConfigMaterial("display.tile." + value, def, def.getAmount());
	}
}
