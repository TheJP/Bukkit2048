package ch.thejp.plugin.game2048;

import org.bukkit.configuration.Configuration;

import ch.thejp.plugin.game2048.logic.GameMode;

/**
 * Interface which allows access to the phrase method
 * @author JP
 *
 */
public interface IConfiguration {
	/**
	 * Gets a localized phrase
	 * @param phrase
	 * @return
	 */
	String getPhrase(String phrase);
	/**
	 * Returns the config of this plugin
	 * @return
	 */
	Configuration getJPConfig();
	/**
	 * Returns the configured GameMode
	 * @return
	 */
	GameMode getGameMode();
}
