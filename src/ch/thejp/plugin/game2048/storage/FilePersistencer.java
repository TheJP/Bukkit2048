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
import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Class to save all game states as binary file in a given folder 
 * @author JP
 */
public class FilePersistencer implements IPersistencer {

	private String path;
	public static final String HIGHSCORE_FILE = "highscore.csv";
	public static final String HIGHSCORE_COL_RANK = "Rang";
	public static final String HIGHSCORE_COL_POINTS = "Punkte";
	public static final String HIGHSCORE_COL_NAME = "Name";
	public static final String ENDING = ".bin"; 

	/**
	 * Constructor with path
	 * @param path Folder in which the items have to be stored
	 */
	public FilePersistencer(String path) {
		//Path has to end with the path separator
		assert path.charAt(path.length()-1) == File.separatorChar : "Invalid path";
		this.path = path;
	}

	@Override
	public void write(IGameState gameState, String itemName) throws IOException {
		DataOutputStream writer = new DataOutputStream(new FileOutputStream(path + itemName + ENDING, false));
		try{
			gameState.write(writer);
		}finally{
			writer.close();
		}
	}

	@Override
	public void read(IGameState gameState, String itemName) throws IOException {
		DataInputStream reader = new DataInputStream(new FileInputStream(path + itemName + ENDING));
		try{
			gameState.read(reader);
		}finally{
			reader.close();
		}
	}

	@Override
	public boolean isAvailable(String itemName) {
		return new File(path + itemName + ENDING).isFile();
	}

	@Override
	public void delete(String itemName) throws IOException {
		File item = new File(path + itemName + ENDING);
		if(item.isFile()){ item.delete(); }
	}

	@Override
	public void readHighscores(HighscoreManager highscores) throws IOException {
		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(path + HIGHSCORE_FILE)));
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
		CSVWriter writer = new CSVWriter(new FileWriter(path + HIGHSCORE_FILE, false));
		try{
			//Write heading
			writer.writeLine(new Object[]{ HIGHSCORE_COL_RANK, HIGHSCORE_COL_POINTS, HIGHSCORE_COL_NAME });
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
