package com.dell.dfs.sfdc;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

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
import com.dell.dfs.sfdc.services.BulkService;
import com.dell.dfs.sfdc.services.IBulkService;
import com.sforce.async.BulkConnection;

public class BulkQuery extends Task implements Observer {

	private String _sObject;
	private String _soqlquery;
	private File _file;
	
	public void setsObject(String sObject) {
        _sObject = sObject;
    }

	public void setSOQLQuery(String soqlquery) {
        _soqlquery = soqlquery;
    }
	
	public void setFile(File file) {
        _file = file;
    }
	
	public void execute() throws BuildException {

		try {

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

			IBulkService bulkService = new BulkService(jobManager, fileManager);
			bulkService.query(_sObject, _soqlquery, _file);

		} catch (Exception e) {
			throw new BuildException(e);
		}
    }

    public void update(Observable observable, Object arg) {
    	log(String.valueOf(arg));
    }
}
