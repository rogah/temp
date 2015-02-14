package com.dell.dfs.sfdc.metadata;

import java.io.File;
import java.io.FilenameFilter;

public class MetadataExtensionFilter implements FilenameFilter {

	private String _extension;
	
	public MetadataExtensionFilter(String extension) {
		_extension = extension;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(String.format(".%s", _extension)) || name.toLowerCase().endsWith("-meta.xml");
	}
	
}
