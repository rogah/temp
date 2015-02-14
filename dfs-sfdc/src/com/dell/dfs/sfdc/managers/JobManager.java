package com.dell.dfs.sfdc.managers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import com.dell.dfs.io.CSVWriter;
import com.dell.dfs.io.ICSVWriter;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.async.QueryResultList;
import com.sforce.ws.util.FileUtil;

import org.apache.commons.lang3.StringUtils;
	
public class JobManager extends Observable implements IJobManager {

	private BulkConnection _connection;

	public JobManager(BulkConnection connection) {
		_connection = connection;
	}

	@Override
	public JobInfo createJob(OperationEnum operation, ContentType contentType, ConcurrencyMode concurrencyMode, String sObjectType) throws AsyncApiException {
		return createJob(operation, contentType, concurrencyMode, sObjectType, null);
	}

	@Override
	public JobInfo createJob(OperationEnum operation, ContentType contentType, ConcurrencyMode concurrencyMode, String sObjectType, String externalIdFieldName) throws AsyncApiException {
		
		JobInfo jobInfo = new JobInfo();
		jobInfo.setObject(sObjectType);
		jobInfo.setOperation(operation);
		jobInfo.setContentType(contentType);
		jobInfo.setConcurrencyMode(concurrencyMode);

		Boolean hasExternalId = (externalIdFieldName != null && externalIdFieldName.length() > 0);

		if (hasExternalId)
			jobInfo.setExternalIdFieldName(externalIdFieldName);

		jobInfo = _connection.createJob(jobInfo);

		notify("Job Id: %s\nJob Content: %s\nJob Operation: %s\nJob SObject: %s", 
			jobInfo.getId(), 
			jobInfo.getContentType(), 
			jobInfo.getOperation(), 
			jobInfo.getObject());

		return jobInfo;
	}

	@Override
	public JobInfo closeJob(String jobInfoId) throws AsyncApiException {
		
		JobInfo jobInfo = new JobInfo();
		jobInfo.setId(jobInfoId);
		jobInfo.setState(JobStateEnum.Closed);

		jobInfo = _connection.updateJob(jobInfo);

		notify("Job Batches: %d", jobInfo.getNumberBatchesTotal());
		
		return jobInfo;
	}

	@Override
	public List<BatchInfo> createBatchesFromCsvFile(JobInfo jobInfo, File file, File transformationSpec) throws IOException, AsyncApiException {

		notify("Job Input File: %s", file.getName());
		
		List<BatchInfo> batchInfos = new ArrayList<BatchInfo>();

		BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				new FileInputStream(file), StandardCharsets.UTF_8));

		String headers = applyTransformationSpec(reader.readLine(), transformationSpec);

		byte[] headerBytes = String.format("%s\n", headers).getBytes(StandardCharsets.UTF_8);
		int headerBytesLength = headerBytes.length;

		File tempFile = File.createTempFile(jobInfo.getId(), ".csv");

		try {
			
			FileOutputStream tempOutput = new FileOutputStream(tempFile);
			
			final int maxBytesPerBatch = 10000000; // 10 million bytes per batch
			final int maxRowsPerBatch = 10000; // 10 thousand rows per batch
			
			int currentBytes = 0;
			int currentLines = 0;
			String nextLine;
			
			while ((nextLine = reader.readLine()) != null) {

				byte[] bytes = String.format("%s\n", nextLine).getBytes(StandardCharsets.UTF_8);
				
				boolean hasReachedSizeLimit = (currentBytes + bytes.length > maxBytesPerBatch) || (currentLines > maxRowsPerBatch);

				if (hasReachedSizeLimit) {
					BatchInfo batchInfo = createBatch(jobInfo, tempFile);
					batchInfos.add(batchInfo);

					currentBytes = 0;
					currentLines = 0;
					tempOutput.flush();
					tempOutput.close();
				}

				if (currentBytes == 0) {
					tempOutput = new FileOutputStream(tempFile);
					tempOutput.write(headerBytes);
					
					currentBytes = headerBytesLength;
					currentLines = 1;
				}

				tempOutput.write(bytes);
				
				currentBytes += bytes.length;
				currentLines++;
			}
			
			if (currentLines > 1) {
				BatchInfo batchInfo = createBatch(jobInfo, tempFile);
				batchInfos.add(batchInfo);
				
				tempOutput.flush();
				tempOutput.close();
			}

		} finally {
			tempFile.delete();
			reader.close();
		}

		return batchInfos;
	}

	@Override
	public BatchInfo createBatchFromQuery(JobInfo jobInfo, String soqlQuery) throws AsyncApiException {
		
		notify("Job Query: %s", soqlQuery);
		
		return _connection.createBatchFromStream(jobInfo, new ByteArrayInputStream(soqlQuery.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public BatchInfo createBatchFromAttachementDir(JobInfo jobInfo, File attachmentDirectory) throws IOException, AsyncApiException {

		List<File> files = FileUtil.listFilesRecursive(attachmentDirectory, false);
		
		Map<String, File> fileMap = new HashMap<String, File>(files.size());

		for (File file : files) {
			fileMap.put(file.getName(), file);
		}

		return _connection.createBatchWithFileAttachments(jobInfo, null, fileMap);
	}

	@Override
	public List<BatchInfo> awaitCompletion(JobInfo jobInfo, List<BatchInfo> batchInfoList) throws AsyncApiException {

		List<BatchInfo> batchResults = new ArrayList<BatchInfo>();

		if (batchInfoList.isEmpty()) 
			return batchResults;

		long sleepTime = 5000L;

		Set<String> batchesQueue = new HashSet<String>();

		for (BatchInfo batchInfo : batchInfoList) {
			batchesQueue.add(batchInfo.getId());
		}

		do {
			BatchInfo[] batchInfoStatuses = _connection.getBatchInfoList(jobInfo.getId()).getBatchInfo();

			for (BatchInfo batchInfo : batchInfoStatuses) {

				notify("Batch: %s (%d|%d). State: %s", batchInfo.getId(), batchInfo.getNumberRecordsProcessed(), batchInfo.getNumberRecordsFailed(), batchInfo.getState());

				if ((batchInfo.getState() == BatchStateEnum.Completed) || (batchInfo.getState() == BatchStateEnum.Failed)) {
					
					if (batchInfo.getState() == BatchStateEnum.Failed) 
						notify("Batch: %s. State: %s. Message: %s", batchInfo.getId(), batchInfo.getState(), batchInfo.getStateMessage());

					if (batchesQueue.remove(batchInfo.getId()))
						batchResults.add(batchInfo);
				}
			}

			try { Thread.sleep(sleepTime); } catch (InterruptedException e) { }
		
		} while (!batchesQueue.isEmpty());

		jobInfo = _connection.getJobStatus(jobInfo.getId());

		notify("Status: Job %s. Batches (%d|%d|%d). Records (%d|%d)",
			jobInfo.getId(), 
			jobInfo.getNumberBatchesCompleted(), 
			jobInfo.getNumberBatchesFailed(),
			jobInfo.getNumberBatchesTotal(),
			jobInfo.getNumberRecordsProcessed(),
			jobInfo.getNumberRecordsFailed());
		
		return batchResults;
	}

	@Override
	public void checkResults(JobInfo jobInfo, List<BatchInfo> batchInfoList, File file) throws AsyncApiException, IOException {

		ICSVWriter writer = null;

		try {

			writer = new CSVWriter(new FileOutputStream(file));

			for (BatchInfo batchInfo : batchInfoList) {

				if (batchInfo.getState() == BatchStateEnum.Failed)
					continue;

				CSVReader reader = new CSVReader(_connection.getBatchResultStream(jobInfo.getId(), batchInfo.getId()));
				List<String> headers = reader.nextRecord();

				writer.write(headers);

				List<String> record;

				while ((record = reader.nextRecord()) != null) {
					
					writer.write(record);

					Map<String, String> results = new HashMap<String, String>();

					for (int i = 0; i < headers.size(); i++) {
						results.put(headers.get(i), record.get(i));
					}

					boolean success = Boolean.valueOf(results.get("Success"));
					if (success) {
						notify("Batch: %s. Record Id: %s", batchInfo.getId(), results.get("Id"));
					} else {
						notify("Batch: %s. ERROR: %s", batchInfo.getId(), results.get("Error"));
					}
				}
			}

		} finally {

			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}

	@Override
	public void checkQueryResults(JobInfo jobInfo, BatchInfo batchInfo, File file) throws AsyncApiException, IOException {
		
		QueryResultList queryResultList = _connection.getQueryResultList(jobInfo.getId(), batchInfo.getId());

		String[] queryResults = queryResultList.getResult();

		if (queryResults != null) {
			
			for (String resultId : queryResults) {

				InputStream inputStream = 
					_connection.getQueryResultStream(jobInfo.getId(), batchInfo.getId(), resultId);

				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				BufferedReader reader = new BufferedReader(
				 	new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
 				
 			  	String line = reader.readLine();
 			  	reader.close();
 				
 				Boolean hasRecords = (line != null && !line.contains("Records not found for this query"));

 				if (!hasRecords) {
 					notify("Records not found for this query.");
 					Files.delete(file.toPath());
 				} else {
 					notify("Output File: %s", file.getName());
 				}
			}
		}
	}

	private BatchInfo createBatch(JobInfo jobInfo, File tempFile) throws IOException, AsyncApiException {
		
		FileInputStream stream = new FileInputStream(tempFile);
		
		BatchInfo batchInfo;

		try {
			batchInfo = _connection.createBatchFromStream(jobInfo, stream);
		} finally {
			stream.close();
		}

		return batchInfo;
	}

	private String applyTransformationSpec(String records, File transformationSpec) throws IOException {

		if (transformationSpec == null)
			return records;

		if (!records.contains(","))
			return records;

		CSVReader reader = new CSVReader(
			new InputStreamReader(
				new FileInputStream(transformationSpec), StandardCharsets.UTF_8));

		Map<String, String> headerMap = new HashMap<String, String>();

		List<String> record;
		while ((record = reader.nextRecord()) != null) {
			headerMap.put(record.get(0), record.get(1));
		}

		List<String> headers = new ArrayList<String>();

		for (String header : records.split(",")) {
			header = sinitizeValue(header);
			if (headerMap.containsKey(header))
				headers.add(headerMap.get(header));
			else
				headers.add(header);
		}

		return StringUtils.join(headers.toArray(new String[headers.size()]), ",");
	}

	private static String sinitizeValue(String value) {
		return value.replaceAll("^\"|\"$", "");
	}

    private void notify(String format, Object... args) {
		setChanged();
		notifyObservers(String.format(format, args)); 
	}

	private void notify(String value) {
		setChanged();
		notifyObservers(value); 
	}
}