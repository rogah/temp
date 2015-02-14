package com.dell.dfs.sfdc;

import java.io.File;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.dell.dfs.properties.IPropertiesFactory;
import com.dell.dfs.properties.PropertiesFactory;
import com.dell.dfs.sfdc.factories.BulkConnectionFactory;
import com.dell.dfs.sfdc.factories.IConnectionFactory;
import com.dell.dfs.sfdc.managers.FileManager;
import com.dell.dfs.sfdc.managers.IFileManager;
import com.dell.dfs.sfdc.managers.IJobManager;
import com.dell.dfs.sfdc.managers.JobManager;
import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.dell.dfs.sfdc.properties.SfdcProperties;
import com.sforce.async.BulkConnection;

public class UpdateFile extends BulkDml {
	
	private File _resultFile;
	private String _fieldsFromFile;
	private String _fieldsToFile;

	public String getFieldsFromFile()
	{
		return (_fieldsFromFile);
	}
	
	public void setFieldsFromFile(String fieldsFromFile)
	{
		_fieldsFromFile = fieldsFromFile;
	}
	
	public String getFieldsToFile()
	{
		return (_fieldsToFile);
	}
	
	public void setFieldsToFile(String fieldsToFile)
	{
		_fieldsToFile = fieldsToFile;
	}
	
	public File getResultFile() {
		return _resultFile;
	}

	public void setResultFile(File resultFile) {
		_resultFile = resultFile;
	}

	@Override
	public void execute() throws BuildException {
		
		try {
			
			if (!isValid()) return;

			IPropertiesFactory factory = new PropertiesFactory();

			ISfdcProperties properies = new SfdcProperties(
				factory.create(getProject().getProperties())
			);

			IConnectionFactory<BulkConnection> connectionFactory = new BulkConnectionFactory(properies);
			connectionFactory.addObserver(this);

			BulkConnection connection = connectionFactory.createConnection();

			IJobManager jobManager = new JobManager(connection);
			jobManager.addObserver(this);
			
			IFileManager fileManager = new FileManager();
			
			Map<String,String> fieldsFromFileToFile = fileManager.buildFieldsMapping(_fieldsFromFile, _fieldsToFile);
			
			fileManager.createUpdatedFile(this.getFile(), _resultFile, fieldsFromFileToFile);

		} catch (Exception e) {
			throw new BuildException(e);
		}
		
	}

	@Override
	protected boolean isValid() {

		if (this.getFile().exists())
			return true;
		
		String message = String.format("File does not exist: \"%s\"", this.getFile().getAbsolutePath());				
		
		if (this.getFailOnFileNotFound()) throw new BuildException(message);
		
		log(message);
		
		return false;
	}
	
}