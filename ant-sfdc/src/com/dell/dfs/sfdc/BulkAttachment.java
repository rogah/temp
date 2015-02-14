package com.dell.dfs.sfdc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
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
import com.dell.dfs.sfdc.services.BulkService;
import com.dell.dfs.sfdc.services.IBulkService;
import com.sforce.async.BulkConnection;

public class BulkAttachment extends BulkDml {

	private File _attachmentDirectory;
	private Collection<String> _fields = new ArrayList<String>();
	private String _attachmentHeaderTemplate;
	private String _attachmentRecordTemplate;
	private File _resultFile;
	
	public void setAttachmentDirectory(File attachmentDirectory) {
        _attachmentDirectory = attachmentDirectory;
    }
	
	public void setFields(String fields) {
		if (fields == null) 
			return;

		fields = fields.replace(" ", "");

		if (!fields.contains(",")) {
			_fields.add(fields);
			return;
		}

		_fields = Arrays.asList(fields.split(","));
    }
	
	public void setAttachmentHeaderTemplate(String attachmentHeaderTemplate) {
        _attachmentHeaderTemplate = attachmentHeaderTemplate;
    }
	
	public void setAttachmentRecordTemplate(String attachmentRecordTemplate) {
        _attachmentRecordTemplate = attachmentRecordTemplate;
    }

	public void setResultFile(File resultFile) {
        _resultFile = resultFile;
    }
	
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

			IBulkService bulkService = new BulkService(jobManager, fileManager);
			
			bulkService.attach(
				this.getsObject(), 
				_attachmentDirectory, 
				this.getFile(), 
				_fields, 
				_attachmentHeaderTemplate, 
				_attachmentRecordTemplate,
				_resultFile);

		} catch (Exception e) {
			throw new BuildException(e);
		}
    }

	@Override
	protected boolean isValid() {
		
		if (!_attachmentDirectory.exists()) {
			String message = String.format("Attachment directory does not exist: \"%s\".", _attachmentDirectory.getAbsolutePath());
			
			if (this.getFailOnFileNotFound()) 
				throw new BuildException(message);
			
			log(message);
			
			return false;
		}
		
		if (!this.getFile().exists()) {
			String message = String.format("Attachment directory does not exist: \"%s\".", this.getFile().getAbsolutePath());
			
			if (this.getFailOnFileNotFound()) 
				throw new BuildException(message);
			
			log(message);
			
			return false;
		}
		
		if (_fields.isEmpty())
			throw new BuildException("Master field(s) not provided.");

		if (StringUtils.isBlank(_attachmentHeaderTemplate))
			throw new BuildException("Attachment Header Template not provided.");

		if (StringUtils.isBlank(_attachmentRecordTemplate))
			throw new BuildException("Attachment Record Template not provided.");
		
		return true;
	}
}
