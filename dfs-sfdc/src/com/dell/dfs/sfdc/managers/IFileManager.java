package com.dell.dfs.sfdc.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.dell.dfs.io.ICSVBuilder;

public interface IFileManager {
	void createRollbackFile(File file) throws IOException;
	File appendFileSuffix(File file, String suffix);
	List<String> getCsvHeaders(File file) throws UnsupportedEncodingException, IOException;
	List<Map<String, String>> getCsvRecords(File file) throws UnsupportedEncodingException, IOException;
	void createCsvFile(File file, InputStream stream) throws IOException;
	void updateFileToInsertRolesRTB(String ParentRoleId, File fileToUpdate) throws IOException;
	ICSVBuilder buildRolesEntries(File file, String id) throws UnsupportedEncodingException, IOException;
	ICSVBuilder updateRoleEntries(File file, String id) throws UnsupportedEncodingException, IOException;
	void createUpdatedFile(File fileToCopyRecords, File fileToUpdate, Map<String,String> recordFromFileToFile) throws UnsupportedEncodingException, IOException;
	File getFileToInsertAutomatedUser(File file, String orgId) throws IOException;
	ICSVBuilder buildFileToUpdateWithMapping(File fileToCopyRecords, File fileToUpdate, Map<String, String> toFileFromFile) throws UnsupportedEncodingException, IOException;
	Map<String,String> buildFieldsMapping(String fieldsFromFile, String fieldsToFile);
	boolean validateFieldsExistOnFile(File file, List<String> fields) throws UnsupportedEncodingException, IOException;
	Map<String,String> getAllValuesFromColumnHeader (File file, String columnHeaderName) throws FileNotFoundException, IOException;
	void applyVLookup(File fileToApply, Map<String,Map<String,String>> recordFieldObject, File... fileToGetRecords) throws FileNotFoundException, IOException;
}
