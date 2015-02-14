package com.dell.dfs.sfdc;

import org.apache.tools.ant.BuildException;

import com.dell.dfs.properties.IPropertiesFactory;
import com.dell.dfs.properties.PropertiesFactory;
import com.dell.dfs.sfdc.factories.BulkConnectionFactory;
import com.dell.dfs.sfdc.factories.IConnectionFactory;
import com.dell.dfs.sfdc.factories.PartnerConnectionFactory;
import com.dell.dfs.sfdc.managers.FileManager;
import com.dell.dfs.sfdc.managers.IFileManager;
import com.dell.dfs.sfdc.managers.IJobManager;
import com.dell.dfs.sfdc.managers.JobManager;
import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.dell.dfs.sfdc.properties.SfdcProperties;
import com.dell.dfs.sfdc.services.BulkService;
import com.dell.dfs.sfdc.services.IBulkService;
import com.dell.dfs.sfdc.services.ISoapService;
import com.dell.dfs.sfdc.services.SoapService;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.PartnerConnection;

public class BulkUserRoles extends BulkDml{

	private String _parentRoleDeveloperNameOrId;
	private boolean _queryParentRoleID;
	
	public String getParentRoleDeveloperNameOrId(){
		return _parentRoleDeveloperNameOrId;
	}
	
	public void setParentRoleDeveloperNameOrId(String parentRoleDeveloperNameOrId){
		_parentRoleDeveloperNameOrId = parentRoleDeveloperNameOrId;
	}
	
	public boolean getQueryParentRoleID(){
		return _queryParentRoleID;
	}
	
	public void setQueryParentRoleID(boolean queryParentRoleID){
		_queryParentRoleID = queryParentRoleID;
	}

	public void execute() throws BuildException {
			
			try {
				
				if (!isValid()) return;
				
				IPropertiesFactory propertiesFactory = new PropertiesFactory();

				ISfdcProperties properies = new SfdcProperties(
					propertiesFactory.create(getProject().getProperties())
				);
				
				IConnectionFactory<PartnerConnection> partnerConnectionFactory = new PartnerConnectionFactory(properies);
				partnerConnectionFactory.addObserver(this);
				
				PartnerConnection partnerConnection = partnerConnectionFactory.createConnection();
				
				ISoapService soapService = new SoapService(partnerConnection);
				soapService.addObserver(this);

				IConnectionFactory<BulkConnection> bulkConnectionFactory = new BulkConnectionFactory(properies);
				bulkConnectionFactory.addObserver(this);

				BulkConnection bulkConnection = bulkConnectionFactory.createConnection();
				
				IJobManager jobManager = new JobManager(bulkConnection);
				jobManager.addObserver(this);

				IFileManager fileManager = new FileManager();
				
				IBulkService bulkService = new BulkService(jobManager, fileManager);

				bulkService.insertNewRole(getFile(), _parentRoleDeveloperNameOrId, _queryParentRoleID);
				
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
