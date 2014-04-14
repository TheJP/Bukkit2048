package ch.thejp.plugin.game2048.storage;

import java.io.IOException;
import java.io.Writer;

/**
 * Class used to write data in the csv format to a stream
 * @author JP
 */
public class CSVWriter {

	private Writer writer;

	/**
	 * @param writer Writer used, to write to the stream
	 */
	public CSVWriter(Writer writer){
		this.writer = writer;
	}

	/**
	 * Writes an array of column values to a line
	 * @param cols
	 * @throws IOException
	 */
	public void writeLine(Object[] cols) throws IOException {
		StringBuilder line = new StringBuilder();
		for(Object col : cols){
			if(col instanceof Number){ line.append((Number)col); }
			else {
				//Escape String columns
				line.append('"');
				line.append(escapeCSV(col.toString()));
				line.append('"');
			}
			line.append(';');
		}
		line.append('\n');
		writer.write(line.toString());
	}

	/**
	 * @see Writer
	 * @throws IOException
	 */
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Escapes the string to be written into a csv file
	 */
	private String escapeCSV(String toEscape){
		return toEscape.replace("\\", "\\\\").replace("\"", "\\\"");
	}

}
