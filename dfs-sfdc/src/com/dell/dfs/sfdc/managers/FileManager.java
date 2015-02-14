package com.dell.dfs.sfdc.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.csvreader.CsvWriter;
import com.dell.dfs.io.CSVBuilder;
import com.dell.dfs.io.CSVWriter;
import com.dell.dfs.io.ICSVBuilder;
import com.dell.dfs.io.ICSVWriter;
import com.sforce.async.CSVReader;

public class FileManager implements IFileManager {
	
	@Override
	public void createRollbackFile(File file) throws IOException {
		CSVReader reader = new CSVReader (new FileInputStream(file));
		
		String csvRollbackFilename = String.format("%s.rollback.csv", 
				file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(".csv")));
		
		CsvWriter csvRollback = new CsvWriter(csvRollbackFilename);
		List<String> row;
		while ((row = reader.nextRecord()) != null) {
			csvRollback.write(String.format("%s", row.get(0)));
			csvRollback.endRecord();
		}
		csvRollback.close();
	}
	
	@Override
	public File appendFileSuffix(File file, String suffix) {
		String csvResultFilename = String.format("%s.%s.csv", 
				file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(".csv")), suffix);
		return new File(csvResultFilename);
	}
	
	public List<String> getCsvHeaders(File file) throws UnsupportedEncodingException, IOException {
		
		FileInputStream stream = null;
		
		try {
			
			stream = new FileInputStream(file);
			
			CSVReader reader = new CSVReader(stream);

			List<String> headers = reader.nextRecord();
						
			return headers;
			
		} finally {
			if (stream != null) stream.close();
		}
	}
	
	public List<Map<String, String>> getCsvRecords(File file) throws UnsupportedEncodingException, IOException {
		
		List<Map<String, String>> records =  new ArrayList<Map<String, String>>();
		
		FileInputStream stream = null;
		
		try {
			
			stream = new FileInputStream(file);
			
			CSVReader reader = new CSVReader(stream);

			List<String> headers = reader.nextRecord();
						
			ArrayList<String> row;
			
			while ((row = reader.nextRecord()) != null) {
				
				Map<String, String> record = new HashMap<String, String>();
				
				for (String header : headers) {
					String value = row.get(headers.indexOf(header));
					if (StringUtils.isBlank(value))
						record.put(header, StringUtils.EMPTY);
					else
						record.put(header, value);
				}
				
				records.add(record);
			}
			
		} finally {
			if (stream != null) stream.close();
		}
		
		return records;
	}

	@Override
	public void createCsvFile(File file, InputStream stream) throws IOException {
		
		ICSVWriter writer = null;
		
		try {
			
			writer = new CSVWriter(new FileOutputStream(file));
			
			CSVReader reader = new CSVReader(stream);
			List<String> record = reader.nextRecord();
			
			writer.write(record);
			
			while ((record = reader.nextRecord()) != null) {
				writer.write(record);
			}
			
			writer.flush();
			
		} finally {
			if (writer != null) writer.close();
		}
	}	
	
	@Override
	public void updateFileToInsertRolesRTB(String ParentRoleNameOrId, File fileToUpdate) throws IOException {
		
		ICSVBuilder builder = buildRolesEntries(fileToUpdate, ParentRoleNameOrId);
		
		createCsvFile(fileToUpdate, builder.toInputStream());
	}
	
	@Override
	public ICSVBuilder buildRolesEntries(File file, String id) throws UnsupportedEncodingException, IOException{
	
	List<Map<String, String>> roles = getCsvRecords(file);
	
	String[] headers = convertHeaderToString(file);
	
	ICSVBuilder builder = new CSVBuilder(headers);
	
	for (Map<String, String> role : roles) {
				
				Map<String, String> record = new HashMap<String, String>();
				
				record.put("ParentRoleId", id);
				record.put("Name", role.get("Name"));
				record.put("MayForecastManagerShare", role.get("MayForecastManagerShare"));
				record.put("RollupDescription", role.get("RollupDescription"));
				record.put("OpportunityAccessForAccountOwner", role.get("OpportunityAccessForAccountOwner"));
				record.put("CaseAccessForAccountOwner", role.get("CaseAccessForAccountOwner"));
				record.put("DeveloperName", role.get("DeveloperName"));
				record.put("ContactAccessForAccountOwner", role.get("ContactAccessForAccountOwner"));
				
				builder.addRecord(record);
		}
	return builder;
	}
	
	@Override
	public ICSVBuilder updateRoleEntries(File file, String id) throws UnsupportedEncodingException, IOException{
	
	List<Map<String, String>> roles = getCsvRecords(file);
	
	String[] headers = convertHeaderToString(file);
	
	ICSVBuilder builder = new CSVBuilder(headers);
	
	for (Map<String, String> role : roles) {
				
				Map<String, String> record = new HashMap<String, String>();
				
				record.put("Id", role.get("Id"));
				record.put("ParentRoleId", id);
				record.put("Name", role.get("Name"));
				record.put("MayForecastManagerShare", role.get("MayForecastManagerShare"));
				record.put("RollupDescription", role.get("RollupDescription"));
				record.put("OpportunityAccessForAccountOwner", role.get("OpportunityAccessForAccountOwner"));
				record.put("CaseAccessForAccountOwner", role.get("CaseAccessForAccountOwner"));
				record.put("DeveloperName", role.get("DeveloperName"));
				record.put("ContactAccessForAccountOwner", role.get("ContactAccessForAccountOwner"));
				
				builder.addRecord(record);
		}
	return builder;
	}
	
	@Override
	public void createUpdatedFile(File fileToCopyRecords, File fileToUpdate, Map<String,String> recordFromFileToFile) throws UnsupportedEncodingException, IOException{

		ICSVBuilder builder;
		
		if(recordFromFileToFile == null || recordFromFileToFile.isEmpty())
		{
			builder = buildFileToUpdate(fileToCopyRecords, fileToUpdate);
		}
		else
		{
			builder = buildFileToUpdateWithMapping(fileToCopyRecords, fileToUpdate, recordFromFileToFile);
		}
		
		createCsvFile(appendFileSuffix(fileToUpdate, "updated"), builder.toInputStream());
		
	}
	
	@Override
	public Map<String,String> buildFieldsMapping(String fieldsFromFile, String fieldsToFile)
	{
		Map<String,String> mapping = new HashMap<>();
		
		List<String> listFieldsFromFile = Arrays.asList(fieldsFromFile.split(","));
		List<String> listFieldsToFile = Arrays.asList(fieldsToFile.split(","));
		
		if(listFieldsFromFile.size() != listFieldsToFile.size())
			throw new IllegalArgumentException("fieldsFromFile and fieldsToFile must have the same number of fields");
		
		for(int i=0; i<listFieldsFromFile.size(); i++)
		{
			mapping.put(listFieldsToFile.get(i), listFieldsFromFile.get(i));
		}
		
		return (mapping);
	}
	
	@Override
	public File getFileToInsertAutomatedUser(File file, String orgId) throws IOException 
	{
		ICSVBuilder builder = buildAutomatedUserOrgIdFile(file, orgId);
		
		File fileUpdatedWithOrgId = appendFileSuffix(file, "updated");
		
		createCsvFile(fileUpdatedWithOrgId, builder.toInputStream());
		
		return (fileUpdatedWithOrgId);
	}

	private ICSVBuilder buildAutomatedUserOrgIdFile(File file, String orgId) throws UnsupportedEncodingException, IOException {
		
		List<Map<String, String>> records = getCsvRecords(file);
		
		List<String> header = getCsvHeaders(file);
		
		Map<String, String> record = records.get(0);
		
		if(record.containsKey("Username"))
		{
			String username = appendUsernameOrgId(record, orgId);
			
			record.put("Username", username);
		}
		
		if(record.containsKey("CommunityNickname"))
		{
			if(!record.get("Username").endsWith(".dfs"))
			{
				record.put("CommunityNickname", appendCommunityNicknameMask(record.get("CommunityNickname")));
			}
		}
		
		ICSVBuilder builder = new CSVBuilder(header);
		
		builder.addRecord(record);
		
		return (builder);
		
	}
	
	private String appendCommunityNicknameMask(String communityNickname)
	{
		Random generator = new Random();
		
		return (communityNickname+String.valueOf(generator.nextInt(99999999)));
	}
	
	private String appendUsernameOrgId(Map<String, String> record, String orgId){
		
		String username = record.get("Username");
		
		String fileOrgId = username.substring(username.indexOf(".dfs")+4, username.length());
		
		if(!(fileOrgId == orgId) || !fileOrgId.equals(orgId))
		{
			username = username.substring(0, username.indexOf(".dfs")+4) + orgId;
		}
		
		return (username);
	}
	
	@Override
	public ICSVBuilder buildFileToUpdateWithMapping(File fileToCopyRecords, File fileToUpdate, Map<String, String> toFileFromFile) throws UnsupportedEncodingException, IOException
	{
		List<Map<String, String>> recordsFromFileResult = getCsvRecords(fileToUpdate);
		
		List<Map<String, String>> recordsToCopy = getCsvRecords(fileToCopyRecords);

		List<String> header = getCsvHeaders(fileToUpdate);

		ICSVBuilder builder = new CSVBuilder(header);
		
		Map<String, String> record = new HashMap<String, String>();
		
		for(Map<String, String> recordFromFileResult : recordsFromFileResult)
		{
			for(Map.Entry<String,String> entry : recordFromFileResult.entrySet())
			{				
				for(Map.Entry<String,String> entryToCopy : toFileFromFile.entrySet())
				{
					if(entryToCopy.getKey().equals(entry.getKey()) || entryToCopy.getKey() == entry.getKey())
					{
						record.put(entryToCopy.getKey(), recordsToCopy.get(0).get(entryToCopy.getValue()));
					}
					else
					{
						record.put(entry.getKey(), entry.getValue());
					}
				}			
			}
			builder.addRecord(record);
			record = new HashMap<String, String>();	
		}
		return builder;
	}

	private ICSVBuilder buildFileToUpdate(File fileToCopyRecords, File fileToUpdate) throws UnsupportedEncodingException, IOException {

		List<Map<String, String>> header2value = getCsvRecords(fileToUpdate);

		List<String> header = getCsvHeaders(fileToUpdate);
		
		List<String> headerFromFileToCopy = getCsvHeaders(fileToCopyRecords);

		ICSVBuilder builder = new CSVBuilder(header);
			
		Map<String, String> record = new HashMap<String, String>();
		
		for (String columnHeaderResult : header) {
			
			if(headerFromFileToCopy.contains(columnHeaderResult))
			{
				record.put(columnHeaderResult, getRecordFromFile(fileToCopyRecords, columnHeaderResult));
			}
			else
			{
				record.put(columnHeaderResult, getRecordFromExistingFile(header2value, columnHeaderResult));
			}
					
		}
		
		builder.addRecord(record);

		return builder;
	}
	
	private String getRecordFromExistingFile(List<Map<String, String>> csvRecords, String target){
		for(Map<String, String> records : csvRecords){
			if(records.containsKey(target)){
				return records.get(target);
			}
		}
		return (StringUtils.EMPTY);
	}
	
	private String[] convertHeaderToString(File file) throws UnsupportedEncodingException, IOException{
		List<String> header = getCsvHeaders(file);
		
		String[] headerToReturn = new String[header.size()];
		
		for(int i =0; i<header.size();i++){
			headerToReturn[i] = header.get(i);
		}
		
		return (headerToReturn);
	}
	
	private String getRecordFromFile(File file, String record) throws IOException{
		CSVReader rdr = new CSVReader(new FileInputStream(file));
		if((rdr.nextRecord()) != null) {

			int index = getHeaderIndex(getCsvHeaders(file), record);
			
			if(index >= 0)
				return (rdr.nextRecord().get(index));
		}
		return ("Record not found!");
	}
	
	private int getHeaderIndex(List<String> header, String target){
		for (int i=0; i<header.size(); i++){
			if(header.get(i).equals(target)){
				return (i);
			}
		}
		return (-1);
	}

	@Override
	public boolean validateFieldsExistOnFile(File file, List<String> fields) throws UnsupportedEncodingException, IOException {
		
		boolean contaisAll = true;
		
		List<String> fileHeader = getCsvHeaders(file);
		
		for(String field : fields)
		{
			if(!fileHeader.contains(field))
			{
				contaisAll = false;
				break;
			}
		}
		
		return contaisAll;
	}

	@Override
	public Map<String, String> getAllValuesFromColumnHeader(File file, String columnHeaderName) throws IOException {

		FileInputStream stream = null;
		
		try {
			
			stream = new FileInputStream(file);
			
			CSVReader reader = new CSVReader (stream);
			
			List<String> row;
	
			Map<String,String> value2header = new HashMap<>();
			
			String value;
			
			List<String> header = getCsvHeaders(file);
			
			if(reader.nextRecord() != null)
				reader.nextRecord();
			
			while ((row = reader.nextRecord()) != null) 
			{
				
				value = row.get(header.indexOf(columnHeaderName));
				
				if (StringUtils.isBlank(value))
					continue;
				else
					if(!value2header.containsKey(value))
						value2header.put(value, columnHeaderName);
			}

			return (value2header);
			
		} finally {
			if (stream != null) stream.close();
		}
	}
	
	
	
	private ICSVBuilder applyVLookupOnFile(File fileToApply, Map<String, Map<String, String>> recordFieldObject, File fileToGetRecords) throws UnsupportedEncodingException, IOException
	{
		ICSVBuilder builder = new CSVBuilder(getCsvHeaders(fileToApply));
		
		List<Map<String,String>> recordsToUpdate = getCsvRecords(fileToApply);
		
		List<Map<String,String>> recordsToCopy = getCsvRecords(fileToGetRecords);
		
		Map<String, String> recordUpdated = new HashMap<String, String>();
		
		for(Map<String,String> row : recordsToUpdate)
		{
			for(String recToUpdate : recordFieldObject.keySet())
			{
				if(row.containsValue(recToUpdate))
				{
					for(Map<String,String> recordToCopy : recordsToCopy)
					{
						for(Map.Entry<String, String> recToCopy : recordToCopy.entrySet())
						{
							if(recToCopy.getValue().equals(recToUpdate)){

								recordUpdated.put(recToUpdate, recordToCopy.get("Id"));

							}
						}
					}
				}
			}
		}
		
		for(Map<String, String> map : recordsToUpdate)
		{
				for(Map.Entry<String, String> updatedRecords : recordUpdated.entrySet())
				{
					if(map.containsValue(updatedRecords.getKey()))
					{
						
						String mapKey = getMapKey(recordsToUpdate, updatedRecords.getKey());
						
						map.put(mapKey, updatedRecords.getValue());
					}
				}
				builder.addRecord(map);
		}
		return (builder);
	}
	
	private String getMapKey(List<Map<String,String>> recordsToUpdate, String value)
	{
		
		for(Map<String, String> map : recordsToUpdate)
		{
			for(Map.Entry<String, String> entry : map.entrySet())
			{
				if(entry.getValue().equals(value))
					return entry.getKey();
			}
		}
		
		return null;
	}
	


	@Override
	public void applyVLookup(File fileToApply, Map<String, Map<String, String>> recordFieldObject, File... fileToGetRecords) throws IOException {
		
		ICSVBuilder builder = null;
		
		File processedFile = appendFileSuffix(fileToApply, "Processed");
		
		createCsvFile(appendFileSuffix(fileToApply, "Processed"), new FileInputStream(fileToApply));
		
		for(int i=0; i<fileToGetRecords.length; i++)
		{
			builder = applyVLookupOnFile(processedFile, recordFieldObject, fileToGetRecords[i]);
			
			createCsvFile(processedFile, builder.toInputStream());
		}

	}

}
