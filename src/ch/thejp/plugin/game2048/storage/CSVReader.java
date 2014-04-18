package ch.thejp.plugin.game2048.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Class used to read data in the csv format from a stream
 * @author JP
 */
public class CSVReader {

	/**
	 * States, which are used, during the readLine method
	 */
	private static enum State { BeforeValue, InUnescapedValue, InEscapedValue, InEscapedValueAfterBackslash, AfterEscapedValue }

	private BufferedReader reader;

	/**
	 * @param reader Reader used to read from the stream
	 */
	public CSVReader(BufferedReader reader) {
		this.reader = reader;
	}

	/**
	 * Read line from csv file
	 * @return A String array with the different columns or null if the end of the stream was reaches
	 * @throws IOException  
	 */
	public String[] readLine() throws IOException {
		ArrayList<String> line = new ArrayList<String>();
		//Read line from string
		String input = reader.readLine();
		if(input == null){ return null; }
		//State machine to unescape
		State state = State.BeforeValue;
		StringBuilder value = new StringBuilder();
		for(char c : input.toCharArray()){
			switch (state) {
				case BeforeValue:
					if(c == '"'){ state = State.InEscapedValue; } //Escaped
					else { value.append(c); state = State.InUnescapedValue; } //Unescaped
					break;
				case InUnescapedValue:
					if(c == ';'){
						line.add(value.toString());
						value = new StringBuilder();
						state = State.BeforeValue; //Next Value
					}
					else { value.append(c); } //Add character, keep state
					break;
				case InEscapedValue:
					if(c == '\\'){ state = State.InEscapedValueAfterBackslash; } //Expecting escaped character
					else if(c == '"'){ state = State.AfterEscapedValue; } //Not in escaped value anymore
					else { value.append(c); } //Add character, keep state
					break;
				case InEscapedValueAfterBackslash:
					//Add character, change state (not expecting escaped charachter anymore)
					value.append(c); state = State.InEscapedValue;
					break;
				case AfterEscapedValue:
					if(c == ';'){
						line.add(value.toString());
						value = new StringBuilder();
						state = State.BeforeValue; //Next Value
					}
					break;
				default: break;
			}
		}
		//Add last value
		if(value.length() > 0){ line.add(value.toString()); }
		String[] array = new String[line.size()];
		return line.toArray(line.toArray(array));
	}

	/**
	 * @see Reader
	 * @throws IOException
	 */
	public void close() throws IOException {
		reader.close();
	}
}
