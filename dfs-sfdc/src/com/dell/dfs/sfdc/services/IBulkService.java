package com.dell.dfs.sfdc.services;

import java.util.*;
import java.io.*;

import com.sforce.async.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.*;

public interface IBulkService {
	void insert(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException;
	void insert(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException;
	void update(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException;
	void update(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException;
	void upsert(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException;
	void upsert(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException;
	void upsert(String sObjectType, String externalIdFieldName, File file) throws ConnectionException, AsyncApiException, IOException;
	void upsert(String sObjectType, String externalIdFieldName, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException;
	void delete(String sObjectType, File file) throws ConnectionException, AsyncApiException, IOException;
	void delete(String sObjectType, File file, File transformationSpec) throws ConnectionException, AsyncApiException, IOException;
	void query(String sObjectType, String soqlQuery, File file) throws ConnectionException, AsyncApiException, IOException;
	void attach(String sObjectType, File attachmentDirectory, File masterFile, Collection<String> masterFields, String headerTemplate, String recordTemplate, File resultFile) throws ConnectionException, AsyncApiException, IOException;
	void insertPricebooks(File fileProducts, List<SObject> standardPriceBooks, List<SObject> currencies, double _unitPrice, boolean useStandardPrice, File filePricebookEntries) throws IOException, ConnectionException, AsyncApiException;
	void insertNewRole(File file, String parentRoleDeveloperNameOrId, boolean queryParentRoleId) throws UnsupportedEncodingException, IOException, ConnectionException, AsyncApiException;
	void updateFileWithParentUserRoleId(File resultFile, String parentDeveloperName) throws ConnectionException, AsyncApiException, IOException;
	void insertAutomatedUser(File file, String orgId) throws IOException, ConnectionException, AsyncApiException;
}
