package com.dell.dfs.sfdc.managers;

import java.util.*;
import java.io.*;

import com.sforce.async.*;

import com.dell.dfs.core.IObservable;

public interface IJobManager extends IObservable {
	JobInfo createJob(OperationEnum operation, ContentType contentType, ConcurrencyMode concurrencyMode, String sObjectType) throws AsyncApiException;
	JobInfo createJob(OperationEnum operation, ContentType contentType, ConcurrencyMode concurrencyMode, String sObjectType, String externalIdFieldName) throws AsyncApiException;
	JobInfo closeJob(String jobInfoId) throws AsyncApiException;

	List<BatchInfo> createBatchesFromCsvFile(JobInfo jobInfo, File file, File transformationSpec) throws IOException, AsyncApiException;
	BatchInfo createBatchFromQuery(JobInfo jobInfo, String soqlQuery) throws AsyncApiException;
	BatchInfo createBatchFromAttachementDir(JobInfo jobInfo, File attachmentDirectory) throws IOException, AsyncApiException;

	List<BatchInfo> awaitCompletion(JobInfo jobInfo, List<BatchInfo> batchInfoList) throws AsyncApiException;
	void checkResults(JobInfo jobInfo, List<BatchInfo> batchInfoList, File file) throws AsyncApiException, IOException;
	void checkQueryResults(JobInfo jobInfo, BatchInfo batchInfo, File file) throws AsyncApiException, IOException;
}