package com.dell.dfs.sfdc.metadata;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface IManifestInfo {

	String getApiVersion();
	Set<String> getTypeNames();
	List<String> getTypeMembers(String typeName);
	String getDirectoryName(String typeName);
	String getFileExtension(String typeName);
	File[] getFiles(String typeName);
	boolean hasMetadaDescription(String typeName);
	boolean hasAllMembers(String typeName);
}
