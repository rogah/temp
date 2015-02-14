package com.dell.dfs.io;

import java.lang.StringBuilder;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Collection;

public class CSVWriter implements ICSVWriter {

	private static final String DELIMITER = ",";
	private static final String ENTRY_FORMAT = "\"%s\"";

	BufferedWriter _writer;

	public CSVWriter(OutputStream outputStream) throws UnsupportedEncodingException {
		_writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
	}

	@Override
	public void write(Collection<String> record) throws IOException {
		_writer.write(join(record));
		_writer.newLine();
	}

	@Override
	public void write(String record) throws IOException {
		_writer.write(record);
		_writer.newLine();
	}

	@Override
	public void flush() throws IOException {
		_writer.flush();
	}

	@Override
	public void close() throws IOException {
		_writer.close();
	}

	private static String join(Collection<String> collection) {

		StringBuilder builder = new StringBuilder();

		Iterator<String> iterator = collection.iterator();
		
		if (iterator.hasNext())
			builder.append(format(iterator.next()));

		while (iterator.hasNext()) {
			builder.append(DELIMITER);
			builder.append(format(iterator.next()));
		}
		
		return builder.toString();
	}

	private static String format(String value) {
		if (value == null)
			return String.format(ENTRY_FORMAT, ""); 
		return String.format(ENTRY_FORMAT, String.valueOf(value)); 
	}
}