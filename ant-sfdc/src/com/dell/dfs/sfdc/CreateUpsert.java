package com.dell.dfs.sfdc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.dell.dfs.io.CSVBuilder;
import com.dell.dfs.io.ICSVBuilder;
import com.dell.dfs.sfdc.managers.FileManager;
import com.dell.dfs.sfdc.managers.IFileManager;

public class CreateUpsert extends Task {

	private File _sourceFile;
	private File _inputFile;
	private String _idFieldName;
	private Collection<String> _lookupFieldNames = new ArrayList<String>();
	private File _outputFile;
	
	public void setSource(File sourceFile) {
		_sourceFile = sourceFile;
	}
	
	public void setInput(File inputFile) {
		_inputFile = inputFile;
	}
	
	public void setOutput(File outputFile) {
		_outputFile = outputFile;
	}
	
	public void setIdField(String idFieldName) {
		_idFieldName = idFieldName;
	}
	
	public void setLookupFields(String lookupFieldNames) {
		if (lookupFieldNames == null) 
			return;

		lookupFieldNames = lookupFieldNames.replace(" ", "");

		if (!lookupFieldNames.contains(",")) {
			_lookupFieldNames.add(lookupFieldNames);
			return;
		}

		_lookupFieldNames = Arrays.asList(lookupFieldNames.split(","));
	}
	
	public void execute() throws BuildException {
		
		try {
			
			if (!isValid()) return;
			
			IFileManager fileManager = new FileManager();
			
			List<String> sourceHeaders = fileManager.getCsvHeaders(_sourceFile);
			List<String> inputHeaders = fileManager.getCsvHeaders(_inputFile);
			
			List<Map<String, String>> sourceRecords = fileManager.getCsvRecords(_sourceFile);
			List<Map<String, String>> inputRecords = fileManager.getCsvRecords(_inputFile);
			
			log(String.format("Source: %s\nInput: %s\nLookup field(s): %s\nCreating upsert file %s.", 
					_sourceFile.getName(),
					_inputFile.getName(),
					StringUtils.join(_lookupFieldNames.toArray(), ", "),
					_outputFile.getName()));
			
			if (!inputHeaders.contains(_idFieldName))
				throw new IllegalArgumentException(
						String.format("Input %s file does not contain %s id field specified.", _inputFile.getName(), _idFieldName));
			
			if (!sourceHeaders.contains(_idFieldName))
				sourceHeaders.add(_idFieldName);
			
			ICSVBuilder builder = new CSVBuilder(sourceHeaders);
			
			int updatedRecords = 0;
			
			for (Map<String, String> sourceRecord : sourceRecords) {
				
				String id = StringUtils.EMPTY;
				
				for (Map<String, String> inputRecord : inputRecords) {
					
					boolean match = true;
					
					for (String lookupField : _lookupFieldNames) {
						match = match && inputRecord.get(lookupField).equals(sourceRecord.get(lookupField));
					}
					
					if (match) {
						id = inputRecord.get(_idFieldName);
						updatedRecords++;
						break;
					}
				}
				
				sourceRecord.put(_idFieldName, id);
				
				builder.addRecord(sourceRecord);
			}
			
			log(String.format("Records with %s field updated: %d of %d", _idFieldName, updatedRecords, sourceRecords.size()));
			
			fileManager.createCsvFile(_outputFile, builder.toInputStream());
			
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	private boolean isValid() {
		
		if (!_sourceFile.exists())
			throw new BuildException(String.format("File does not exist: \"%s\"", _sourceFile.getAbsolutePath()));
		
		if (!_inputFile.exists())
			throw new BuildException(String.format("File does not exist: \"%s\"", _inputFile.getAbsolutePath()));
		
		if (StringUtils.isBlank(_idFieldName))
			throw new BuildException("Id field name must be provided.");
		
		if (_lookupFieldNames.size() == 0)
			throw new BuildException("At least one lookup field name must be provided.");
		
		return true;
	}
}
