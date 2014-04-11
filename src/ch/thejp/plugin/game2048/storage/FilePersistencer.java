package ch.thejp.plugin.game2048.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.thejp.plugin.game2048.logic.IGameState;

/**
 * Class to save all game states as binary file in a given folder 
 * @author JP
 */
public class FilePersistencer implements IPersistencer {

	private String path;
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
}
