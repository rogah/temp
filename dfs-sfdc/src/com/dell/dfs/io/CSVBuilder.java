package com.dell.dfs.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CSVBuilder implements ICSVBuilder {

	private static final String DELIMITER = ",";
	private static final String ENTRY_FORMAT = "\"%s\"";

	private List<String> _headers = new LinkedList<String>();
	private List<Map<Integer, String>> _records = new LinkedList<Map<Integer, String>>();

	public CSVBuilder(List<String> headers) {
		this(headers.toArray(new String[headers.size()]));
	}
	
	public CSVBuilder(String... headers) {
		for (String header : headers)
            _headers.add(header);
	}

	@Override
	public void addHeader(String headerName) {
		if (!_headers.contains(headerName)) {
			_headers.add(headerName);

			Integer index = _headers.indexOf(headerName);

			for (Map<Integer, String> record : _records) {
				if (!record.containsKey(index))
					record.put(index, "");
			}
		}
	}

	@Override
	public void removeHeader(String headerName) {
		if (_headers.contains(headerName)) {
			Integer index = _headers.indexOf(headerName);

			for (Map<Integer, String> record : _records) {
				if (record.containsKey(index))
					record.remove(index);
			}

			_headers.remove(index);
		}
	}

	@Override
	public void renameHeader(String originalName, String newName) {
		if (_headers.contains(originalName))
			_headers.set(_headers.indexOf(originalName), newName);
	}

	@Override
	public boolean containsHeader(String headerName) {
		return _headers.contains(headerName);
	}

	@Override
	public Iterator<String> getHeaders() {
		return _headers.iterator();
	}

	@Override
	public void addRecord(Map<String, String> entries) {
		
		Map<Integer, String> record = new HashMap<Integer, String>();
		
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			if (_headers.contains(entry.getKey())) {
				Integer index = _headers.indexOf(entry.getKey());
				record.put(index, entry.getValue());
			}
		}
		
		_records.add(record);
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();

		Iterator<String> headers = _headers.iterator();
		Iterator<Map<Integer, String>> records = _records.iterator();

		if (headers.hasNext())
			buffer.append(format(headers.next()));

		while (headers.hasNext()) {
			buffer.append(DELIMITER);
			buffer.append(format(headers.next()));
		}

		buffer.append("\n");

		while (records.hasNext()) {
			Map<Integer, String> record = records.next();

			Iterator<Map.Entry<Integer, String>> entries = record.entrySet().iterator();

			if (entries.hasNext())
				buffer.append(format(entries.next().getValue()));

			while (entries.hasNext()){
				buffer.append(DELIMITER);
				buffer.append(format(entries.next().getValue()));
			}

			buffer.append("\n");
		}

		return buffer.toString();
	}

	@Override
	public InputStream toInputStream() {
		return new ByteArrayInputStream(toString().getBytes(StandardCharsets.UTF_8));
	}

	private static String format(String value) {
		if (value == null)
			return String.format(ENTRY_FORMAT, ""); 
		return String.format(ENTRY_FORMAT, String.valueOf(value)); 
	}
}