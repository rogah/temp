package com.dell.dfs.sfdc;

import java.io.File;
import java.util.List;

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
import com.sforce.soap.partner.sobject.SObject;

public class BulkPricebook extends BulkDml {
	
	private String _soqlStandardPriceBooks;
	private String _soqlCurrencies;
	private double _unitPrice;
	private boolean _useStandardPrice;
	private File _resultFile;
	
	public void setSoqlStandardPriceBooks(String soqlStandardPriceBooks) {
		_soqlStandardPriceBooks = soqlStandardPriceBooks;
	}
	
	public void setSoqlCurrencies(String soqlCurrencies) {
		_soqlCurrencies = soqlCurrencies;
	}
	
	public void setUnitPrice(double unitPrice) {
		_unitPrice = unitPrice;
	}
	
	public void setUseStandardPrice(boolean useStandardPrice) {
		_useStandardPrice = useStandardPrice;
	}
	
	public void setResultFile(File resultFile) {
		_resultFile = resultFile;
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
			
			List<SObject> standardPriceBooks = soapService.query(_soqlStandardPriceBooks);
			List<SObject> currencies = soapService.query(_soqlCurrencies);

			IConnectionFactory<BulkConnection> bulkConnectionFactory = new BulkConnectionFactory(properies);
			bulkConnectionFactory.addObserver(this);

			BulkConnection bulkConnection = bulkConnectionFactory.createConnection();
			
			IJobManager jobManager = new JobManager(bulkConnection);
			jobManager.addObserver(this);

			IFileManager fileManager = new FileManager();
			
			IBulkService bulkService = new BulkService(jobManager, fileManager);
			
			bulkService.insertPricebooks(
					this.getFile(), 
					standardPriceBooks, 
					currencies, 
					_unitPrice, 
					_useStandardPrice, 
					_resultFile);
			
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
