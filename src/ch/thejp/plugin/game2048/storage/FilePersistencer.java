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
import ch.thejp.plugin.game2048.IConfiguration;
import ch.thejp.plugin.game2048.logic.GameMode;
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Class to save all game states as binary file in a given folder 
 * @author JP
 */
public class FilePersistencer implements IPersistencer {

	private String path;
	private String highscoreFile;
	private String highscoreColRank = "Rang";
	private String highscoreColPoints = "Punkte";
	private String highscoreColName = "Name";
	//Changed in version 2.1.0 because saves are not backward compatible
	//Another change in version 2.1.3 so different gamemode save files do not ever mix up
	private String ending;
	private final static String BACKUP_ENDING = ".bak";

	/**
	 * Constructor with path
	 * @param path Folder in which the items have to be stored
	 */
	public FilePersistencer(String path, IConfiguration config) {
		//Path has to end with the path separator
		assert path.charAt(path.length()-1) == File.separatorChar : "Invalid path";
		this.path = path;
		//Highscore Filename
		this.highscoreFile = config.getJPConfig().getString("storage.highscore-file", "hs.csv");
		//Highscore headings
		this.highscoreColRank = config.getPhrase("hs-rank");
		this.highscoreColPoints = config.getPhrase("hs-score");
		this.highscoreColName = config.getPhrase("hs-name");
		//Different endings for 64 and 2048 gamemode
		ending = config.getGameMode() == GameMode.GM64 ? ".s64" : ".sav";
	}

	@Override
	public void write(IGameState gameState, String itemName) throws IOException {
		File file = new File(path + itemName + ending);
		//Create backup for undo operation
		if(file.exists()){
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
		DataInputStream reader = new DataInputStream(new FileInputStream(path + itemName + ending));
		try{
			gameState.read(reader);
		}finally{
			reader.close();
		}
	}

	@Override
	public boolean isAvailable(String itemName) {
		return new File(path + itemName + ending).isFile();
	}

	@Override
	public void delete(String itemName) throws IOException {
		File item = new File(path + itemName + ending);
		if(item.isFile()){ item.delete(); }
	}

	@Override
	public void readHighscores(HighscoreManager highscores) throws IOException {
		File highscoreCheckFile = new File(path + highscoreFile);
		if(!highscoreCheckFile.exists()){ return; }
		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(path + highscoreFile)));
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
		CSVWriter writer = new CSVWriter(new FileWriter(path + highscoreFile, false));
		try{
			//Write heading
			writer.writeLine(new Object[]{ highscoreColRank, highscoreColPoints, highscoreColName });
			//Entry<String, Long>[] entries = highscores.getSorted();
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
