package com.dell.dfs.sfdc.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.dell.dfs.io.CSVBuilder;
import com.dell.dfs.io.CSVWriter;
import com.dell.dfs.io.ICSVBuilder;
import com.dell.dfs.io.ICSVWriter;
import com.dell.dfs.sfdc.managers.IFileManager;
import com.dell.dfs.sfdc.managers.IJobManager;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.OperationEnum;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public class BulkService implements IBulkService {
	
	private IJobManager _jobManager;
	private IFileManager _fileManager;
	
	public BulkService(IJobManager jobManager, IFileManager fileManager) {
		_jobManager = jobManager;
		_fileManager = fileManager;
	}

	@Override
	public void insert(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException {
		insert(sObjectType, file, null);
	}

	@Override
	public void insert(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.insert, 
			ContentType.CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType);
		
		File resultFile = process(jobInfo, file, transformationSpec);
		
		_fileManager.createRollbackFile(resultFile);
	}

	@Override
	public void update(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException {
		update(sObjectType, file, null);
	}

	@Override
	public void update(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.update, 
			ContentType.CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType);

		process(jobInfo, file, transformationSpec);
	}

	@Override
	public void upsert(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException {
		upsert(sObjectType, file, null);
	}

	@Override
	public void upsert(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {
		upsert(sObjectType, null, file, null);
	}

	@Override
	public void upsert(String sObjectType, String externalIdFieldName, File file) throws ConnectionException, AsyncApiException, IOException {
		upsert(sObjectType, externalIdFieldName, file, null);
	}

	@Override
	public void upsert(String sObjectType, String externalIdFieldName, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.upsert, 
			ContentType.CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType,
			externalIdFieldName);

		process(jobInfo, file, transformationSpec);
	}

	@Override
	public void delete(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException {
		delete(sObjectType, file, null);
	}

	@Override
	public void delete(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.delete, 
			ContentType.CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType);

		process(jobInfo, file, transformationSpec);
	}

	@Override
	public void query(String sObjectType, String soqlQuery, File file) throws ConnectionException, AsyncApiException, IOException {
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.query, 
			ContentType.CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType);

		processQuery(jobInfo, soqlQuery, file);
	}

	@Override
	public void attach(String sObjectType, File attachmentDirectory, File masterFile, Collection<String> masterFields, String headerTemplate, String recordTemplate, File resultFile) throws ConnectionException, AsyncApiException, IOException {		
		
		JobInfo jobInfo = _jobManager.createJob(
			OperationEnum.insert, 
			ContentType.ZIP_CSV, 
			ConcurrencyMode.Parallel, 
			sObjectType);

		createAttachmentManifest(
			attachmentDirectory, 
			masterFile, 
			masterFields, 
			headerTemplate, 
			recordTemplate);

		processAttachment(jobInfo, attachmentDirectory, resultFile);
	}
	
	@Override
	public void updateFileWithParentUserRoleId(File resultFile, String parentDeveloperName) throws ConnectionException, AsyncApiException, IOException{
		
		String queryParentId = "SELECT Id FROM UserRole WHERE DeveloperName = '" + parentDeveloperName + "'";
		
		File fileWithParentRoleId = _fileManager.appendFileSuffix(resultFile, parentDeveloperName);
		
		query("UserRole", queryParentId, fileWithParentRoleId);
		
		String id = getIdFromRole(fileWithParentRoleId);
		
		File copyFromResultFile = _fileManager.appendFileSuffix(resultFile, "copy");
		
		_fileManager.createCsvFile(copyFromResultFile, new FileInputStream(resultFile));
		
		ICSVBuilder builder = _fileManager.updateRoleEntries(copyFromResultFile, id);
		
		_fileManager.createCsvFile(copyFromResultFile, builder.toInputStream());
	}
	
	@Override
	public void insertNewRole(File file, String parentRoleDeveloperNameOrId, boolean queryParentRoleId) throws UnsupportedEncodingException, IOException, ConnectionException, AsyncApiException {
		
		String id;
		
		if(!queryParentRoleId)
			id = parentRoleDeveloperNameOrId;
		else{
			
			String queryId = "SELECT Id FROM UserRole WHERE DeveloperName = '" + parentRoleDeveloperNameOrId + "'";
			
			File fileWithParentRoleId = _fileManager.appendFileSuffix(file, parentRoleDeveloperNameOrId);
			
			query("UserRole", queryId, fileWithParentRoleId);
			
			id = getIdFromRole(fileWithParentRoleId);
		}
		
		File copyFromFile = _fileManager.appendFileSuffix(file, "copy");
		
		_fileManager.createCsvFile(copyFromFile, new FileInputStream(file));
		
		_fileManager.updateFileToInsertRolesRTB(id, copyFromFile);
		
		insert("UserRole", copyFromFile);
	}
	
	private String getIdFromRole(File file) throws IOException{
		CSVReader rdr = new CSVReader(new FileInputStream(file));
		if((rdr.nextRecord()) != null) {
			return (rdr.nextRecord().get(0));
		}
		return ("IDnotFound");
	}
	
	@Override
	public void insertPricebooks(File fileProducts, List<SObject> standardPriceBooks, List<SObject> currencies, double unitPrice, boolean useStandardPrice, File filePricebookEntries) throws IOException, ConnectionException, AsyncApiException {
		
		ICSVBuilder builder = buildPricebookEntries(
				fileProducts, standardPriceBooks, currencies, unitPrice, useStandardPrice);
		
		_fileManager.createCsvFile(filePricebookEntries, builder.toInputStream());
		
		insert("PricebookEntry", filePricebookEntries);
	}

	private File process(JobInfo jobInfo, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException {

		List<BatchInfo> batches = _jobManager.createBatchesFromCsvFile(jobInfo, file, transformationSpec);

		jobInfo = _jobManager.closeJob(jobInfo.getId());
		
		List<BatchInfo> batchResults = _jobManager.awaitCompletion(jobInfo, batches);

		File resultFile = _fileManager.appendFileSuffix(file, "results");
		
		_jobManager.checkResults(jobInfo, batchResults, resultFile);
		
		return resultFile;
	}

	private void processQuery(JobInfo jobInfo, String soqlQuery, File file) throws ConnectionException, AsyncApiException, IOException {

		BatchInfo batchInfo = _jobManager.createBatchFromQuery(jobInfo, soqlQuery);

		jobInfo = _jobManager.closeJob(jobInfo.getId());

		_jobManager.awaitCompletion(jobInfo, Arrays.asList(batchInfo));

		_jobManager.checkQueryResults(jobInfo, batchInfo, file);
	}

	private void processAttachment(JobInfo jobInfo, File attachmentDirectory, File resultFile) throws ConnectionException, AsyncApiException, IOException {

		BatchInfo batchInfo = _jobManager.createBatchFromAttachementDir(jobInfo, attachmentDirectory);

		jobInfo = _jobManager.closeJob(jobInfo.getId());

		List<BatchInfo> batchResults = _jobManager.awaitCompletion(jobInfo, Arrays.asList(batchInfo));
			
		_jobManager.checkResults(jobInfo, batchResults, resultFile);
	}

	private void createAttachmentManifest(File attachmentDirectory, File masterFile, Collection<String> masterFields, String headerTemplate, String recordTemplate) throws IOException {
		
		File manifastFile = new File(attachmentDirectory, "request.txt");

		CSVReader reader = new CSVReader(
			new InputStreamReader(
				new FileInputStream(masterFile), StandardCharsets.UTF_8));

		List<String> headers = reader.nextRecord();

		ICSVWriter writer = new CSVWriter(new FileOutputStream(manifastFile));
		writer.write(headerTemplate);

		List<String> record;
		
		while ((record = reader.nextRecord()) != null) {

			List<String> entries = new ArrayList<String>();

			for (String masterField : masterFields) {
				Integer index = headers.indexOf(masterField);
				entries.add(record.get(index));
			}

			writer.write(String.format(recordTemplate, entries.toArray()));
		}

		writer.flush();
		writer.close();
	}
	
	private ICSVBuilder buildPricebookEntries(File fileProducts, List<SObject> standardPriceBooks, List<SObject> currencies, double unitPrice, boolean useStandardPrice) throws UnsupportedEncodingException, IOException {
		
		List<Map<String, String>> products = _fileManager.getCsvRecords(fileProducts);
		
		String[] headers = new String[] {
				"CurrencyIsoCode", "IsActive", "Pricebook2Id", "Product2Id", "UnitPrice", "UseStandardPrice"};
		
		ICSVBuilder builder = new CSVBuilder(headers);
		
		for (SObject standardPriceBook : standardPriceBooks) {
			
			for (SObject currency : currencies) { 
				
				for (Map<String, String> product : products) {
					
					String productId = product.get("Id");
					
					if (StringUtils.isBlank(productId)) continue;
					
					Map<String, String> record = new HashMap<String, String>();
					record.put("CurrencyIsoCode", String.valueOf(currency.getField("IsoCode")));
					record.put("IsActive", "True");
					record.put("Pricebook2Id", String.valueOf(standardPriceBook.getField("Id")));
					record.put("Product2Id", productId);
					record.put("UnitPrice", String.valueOf(unitPrice));
					record.put("UseStandardPrice", String.valueOf(useStandardPrice));
					
					builder.addRecord(record);
				}
			}
		}
		
		return builder;
	}
	
	@Override
	public void insertAutomatedUser(File file, String orgId) throws IOException, ConnectionException, AsyncApiException
	{
		File fileUpdatedToInsert = _fileManager.getFileToInsertAutomatedUser(file, orgId);
		
		insert("User", fileUpdatedToInsert);
	}

}
