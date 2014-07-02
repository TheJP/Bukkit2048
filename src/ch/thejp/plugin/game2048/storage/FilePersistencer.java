package ch.thejp.plugin.game2048.storage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import ch.thejp.plugin.game2048.HighscoreManager;
import ch.thejp.plugin.game2048.JPConfiguration;
import ch.thejp.plugin.game2048.logic.GameMode;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Class to save all game states as binary file in a given folder 
 * @author JP
 */
public class FilePersistencer implements IPersistencer {

	private String path = "";
	private final static String BACKUP_ENDING = ".bak";
	JPConfiguration config = null;

	/**
	 * Constructor with path
	 * @param path Folder in which the items have to be stored
	 */
	public FilePersistencer(JPConfiguration config) {
		this.config = config;
	}

	/**
	 * Generates the correct ending according to the config.
	 * Different endings for 64 and 2048 gamemode
	 * @return Ending in the format ".eee"
	 */
	protected String getEnding() {
		return config.getGameMode() == GameMode.GM64 ? ".s64" : ".sav";
	}

	/**
	 * Generates the correct path according to the config
	 * @return
	 */
	protected String getPath() {
		File storage = new File(config.getStoragePath());
		String path = storage.getAbsolutePath();
		if(!this.path.equals(path)){
			storage.mkdirs(); //Create folder structure if it doesn' exist
			this.path = path;
		}
		return path;
	}

	@Override
	public void write(IGameState gameState, String itemName) throws IOException {
		write(gameState, itemName, false);
	}

	@Override
	public void write(IGameState gameState, String itemName, boolean backup) throws IOException {
		File file = new File(getPath(), itemName + getEnding());
		//Create backup for undo operation
		if(backup && file.isFile()){
			File bakFile = new File(file.getCanonicalPath() + BACKUP_ENDING);
			if(!bakFile.exists() || bakFile.delete()){
				file.renameTo(bakFile);
			}
		}
		//Save new game state
		DataOutputStream writer = new DataOutputStream(new FileOutputStream(file, false));
		try{
			gameState.write(writer);
		}finally{
			writer.close();
		}
	}

	@Override
	public void read(IGameState gameState, String itemName) throws IOException {
		DataInputStream reader = new DataInputStream(new FileInputStream(new File(getPath(), itemName + getEnding())));
		try{
			gameState.read(reader);
		}finally{
			reader.close();
		}
	}

	@Override
	public void undo(String itemName, boolean unlimited) throws IOException {
		File file = new File(getPath(), itemName + getEnding());
		File bakFile = new File(file.getCanonicalPath() + BACKUP_ENDING);
		//Replace current file with backup
		if(bakFile.isFile()){
			if(!file.isFile() || file.delete()){
				bakFile.renameTo(file);
			}
		}
	}

	@Override
	public boolean isAvailable(String itemName, boolean backup) {
		return new File(getPath(), itemName + getEnding() + (backup ? BACKUP_ENDING : "")).isFile();
	}

	@Override
	public boolean isAvailable(String itemName) {
		return isAvailable(itemName, false);
	}

	@Override
	public void delete(String itemName) throws IOException {
		File item = new File(getPath(), itemName + getEnding());
		if(item.isFile()){ item.delete(); }
		File bakFile = new File(item.getCanonicalPath() + BACKUP_ENDING);
		if(bakFile.isFile()){ bakFile.delete(); }
	}

	@Override
	public void readHighscores(HighscoreManager highscores) throws IOException {
		String path = getPath(); //Load path from config
		String hsFile = config.getHighscoreFile();
		File highscoreCheckFile = new File(path, hsFile);
		if(!highscoreCheckFile.exists()){ return; }
		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(highscoreCheckFile)));
		try{
			reader.readLine(); //Read Headings
			String[] values;
			do {
				values = reader.readLine();
				if(values != null){
					try{
						highscores.set(values[2], Long.valueOf(values[1]));
					} catch(Throwable e) { throw new IOException(e); } //Pass exception up
				}
			} while(values != null);
		}
		finally{
			reader.close();
		}
	}

	@Override
	public void writeHighscores(HighscoreManager highscores) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(new File(getPath(), config.getHighscoreFile()), false));
		try{
			//Write heading
			writer.writeLine(new Object[]{ config.getPhrase("hs-rank"), config.getPhrase("hs-score"), config.getPhrase("hs-name") });
			int rank = 0, lastRank = 1; long lastScore = Long.MAX_VALUE;
			for(Entry<String, Long> row : highscores.getSorted()){
				++rank;
				if(row.getValue() < lastScore){ lastRank = rank; lastScore = row.getValue(); }
				writer.writeLine(new Object[]{ lastRank, lastScore, row.getKey() });
			}
		}finally{
			writer.close();
		}
	}
}
