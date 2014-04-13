package ch.thejp.plugin.game2048;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to manage the highscore of the players
 * @author JP
 *
 */
public class HighscoreManager {

	Map<String, Long> highscores = new HashMap<String, Long>();

	/**
	 * Returns the highscore of the given player or -1 if no highscore was found
	 * @param playerName
	 * @return
	 */
	public long get(String playerName){
		//Don't use auto boxing, because Long could be null
		Long highscore = highscores.get(playerName);
		return  (highscore == null ? -1 : highscore.longValue());
	}

	/**
	 * Sets the highscore of the given player.
	 * Only if the given highscore is higher the the existing
	 * @param playerName
	 * @param highscore
	 */
	public void set(String playerName, long highscore){
		//Add new highscore: if the player has no highscore or if the new highscore is higher then the old
		if(!highscores.containsKey(playerName) || highscores.get(playerName).longValue() < highscore){
			highscores.put(playerName, highscore);
		}
	}

	/**
	 * Returns the sorted highscores
	 * @return
	 */
	public Entry<String, Long>[] getSorted(){
		@SuppressWarnings("unchecked")
		Entry<String, Long>[] result = (Entry<String, Long>[]) highscores.entrySet().toArray();
		Arrays.sort(result, new Comparator<Entry<String, Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return result;
	}
}
