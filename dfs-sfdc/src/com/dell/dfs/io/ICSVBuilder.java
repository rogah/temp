package com.dell.dfs.io;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public interface ICSVBuilder {
	void addHeader(String headerName);
	void removeHeader(String headerName);
	void renameHeader(String originalName, String newName);
	boolean containsHeader(String headerName);
	Iterator<String> getHeaders();
	void addRecord(Map<String, String> entries);
	@Override
	String toString();
	InputStream toInputStream();
}