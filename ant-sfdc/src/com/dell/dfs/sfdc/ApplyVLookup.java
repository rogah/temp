package com.dell.dfs.sfdc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ApplyVLookup extends BulkDml {

	/*
	 * only the lookup fields present in the input file separated by comma
	 */
	private String _vLookupFields;
	/*
	 * parent object for each lookup field above separated by comma (in the same order as the fields)
	 * for example, <First Object>,<Second Object>
	 * the <First Object> will be mapped to the first field declared on _vLookupFields and so on
	 */
	private String _parentObject;
	/*
	 * actual name of the lookup fields in the object to query
	 * for example, <First Field>,<Second Field>
	 * the <First Field> will be mapped to the first object declared on _parentObject and so on
	 */
	private String _fieldObjectCriteriaMap;
	/*
	 * extra criteria to the query
	 * use <Object Name> separated by ';;;' and followed by each extra criteria then --- to the
	 * next objects and criterias
	 * example: Product2;;;AND Product_Type__c NOT IN ('Other','Dell','Non-Dell') --- <Other Object> ;;; <Other criteria>
	 */
	private String _extraCriteriaQuery;
	
	public String getExtraCriteriaQuery()
	{
		return (_extraCriteriaQuery);
	}
	
	public void setExtraCriteriaQuery(String extraCriteriaQuery)
	{
		_extraCriteriaQuery = extraCriteriaQuery;
	}
	
	public String getFieldObjectCriteriaMap()
	{
		return (_fieldObjectCriteriaMap);
	}
	
	public void setFieldObjectCriteriaMap(String fieldObjectCriteriaMap)
	{
		_fieldObjectCriteriaMap = fieldObjectCriteriaMap;
	}
	
	public String getVLookupFields()
	{
		return _vLookupFields;
	}
	
	public void setVLookupFields(String vLookupFields)
	{
		_vLookupFields = vLookupFields;
	}
	
	public String getParentObject()
	{
		return _parentObject;
	}
	
	public void setParentObject(String parentObject)
	{
		_parentObject = parentObject;
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

			IBulkService bulkService = new BulkService(jobManager, fileManager);
			
			List<String> vLookupFields = Arrays.asList(_vLookupFields.split(","));
			
			List<String> parentObjects = Arrays.asList(_parentObject.split(","));
						
			if(!fileManager.validateFieldsExistOnFile(getFile(), vLookupFields))
				throw new IllegalArgumentException("All vLookupFields must exist on file header");
			
			Map<String,String> fieldToParentObject = fileManager.buildFieldsMapping(_parentObject, _vLookupFields);
			
			Map<String,String> fieldToQueryCriteria = new HashMap<String, String>();
			
			if(_fieldObjectCriteriaMap.trim().isEmpty())
			{
				fieldToQueryCriteria = fileManager.buildFieldsMapping(_vLookupFields, _vLookupFields);
			}
			else
			{
				fieldToQueryCriteria = fileManager.buildFieldsMapping(_fieldObjectCriteriaMap, _vLookupFields);
			}
			
			List<Map<String,String>> recordsToQuery = new ArrayList<>();
			
			for(String field : vLookupFields)
				recordsToQuery.add(fileManager.getAllValuesFromColumnHeader(getFile(), field));
			
			Map<String,Map<String,String>> recordFieldObject = populateRecordFieldObject(recordsToQuery, fieldToParentObject);
			
			Map<String, String> objectToExtraCriteriaQuery = objectToExtraCriteriaQuery(_extraCriteriaQuery);
			
			List<String> queries = buildQueriesList(parentObjects, recordFieldObject, recordsToQuery, fieldToQueryCriteria, objectToExtraCriteriaQuery);
			
			File[] files;
			
			if(!queries.isEmpty())
			{
				
				files = new File[queries.size()];
				
				for(int i=0; i<queries.size(); i++)
	        	{
					String query = queries.get(i);
					
	        		String queryObject = query.substring(query.indexOf("FROM "), query.length()).split(" ")[1];
	        		
	        		File file = fileManager.appendFileSuffix(getFile(), queryObject);
	        		
	        		files[i] = file;
	        		
	        		bulkService.query(queryObject, query, file);
	        	}
				
				fileManager.applyVLookup(this.getFile(), recordFieldObject, files);
				
			}
			else
			{
				throw new Exception("There was a problem generating the queries");
			}

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
	
	private Map<String,Map<String,String>> populateRecordFieldObject(List<Map<String,String>> recordsToQuery, Map<String,String> fieldToParentObject)
	{
		
		Map<String,Map<String,String>> recordFieldObject = new HashMap<>();
		
		for(Map<String,String> recordField : recordsToQuery)
		{
			for(Map.Entry<String,String> entry : recordField.entrySet())
			{
				
					String recordKey = entry.getKey();
					
					String valueToMap = entry.getValue();
					
					if((!recordFieldObject.containsKey(recordKey)) && (fieldToParentObject.containsKey(valueToMap)))
					{
						Map<String,String> aux = new HashMap<>();
						
						aux.put(valueToMap, fieldToParentObject.get(valueToMap));
						
						recordFieldObject.put(recordKey, aux);
					}			
			}
		}
		
		return (recordFieldObject);
	}
	
	private Map<String, String> objectToExtraCriteriaQuery(String extraCriteriaQuery)
	{
		Map<String, String> objToExtraCriteria = new HashMap<String, String>();
		
		if(!extraCriteriaQuery.trim().isEmpty())
		{
			String[] extraCriterias = extraCriteriaQuery.split("---");
			
			if(extraCriterias.length > 0)
			{
				for(int i=0; i<extraCriterias.length; i++)
				{
					String obj = extraCriterias[i].split(";;;")[0];
					String extraCriteria = extraCriterias[i].split(";;;")[1];
					
					if(!obj.trim().isEmpty() && !extraCriteria.trim().isEmpty())
					{
						objToExtraCriteria.put(obj, extraCriteria);
					}
					
				}
			}
			
		}
		
		return (objToExtraCriteria);
	}
		
	private List<String> buildQueriesList(List<String> parentObjects, Map<String, Map<String,String>> recordFieldObject, List<Map<String, String>> recordToField, Map<String,String> fieldToQueryCriteria, Map<String, String> extraCriteriaToObj)
	{

		List<String> queries = new ArrayList<String>();

		Set<String> objects = new HashSet<String>();

		Set<String> fieldsToAdd = new HashSet<String>();

		Set<String> recordsToAdd = new HashSet<String>();

		objects.addAll(parentObjects);

		StringBuilder qBuilder = new StringBuilder();

		for (String object : objects) {

			qBuilder.append("SELECT Id,");

			for (Map<String, String> fieldToParentObject : recordFieldObject.values()) {

				for (Map.Entry<String, String> field : fieldToParentObject.entrySet()) {
					if (field.getValue() == object || field.getValue().equals(object)) {
						if(fieldToQueryCriteria.containsKey(field.getKey()))
						{
							fieldsToAdd.add(fieldToQueryCriteria.get(field.getKey()) + ",");
						}
					}
				}
			}

			for (String field : fieldsToAdd)
				qBuilder.append(field);

			qBuilder.deleteCharAt(qBuilder.length() - 1);

			String field = fieldsToAdd.iterator().next();

			String criteria = (field.substring(0, field.length() - 1));
			
			if(fieldToQueryCriteria.containsKey((field.substring(0, field.length() - 1))))
			{
				criteria = fieldToQueryCriteria.get((field.substring(0, field.length() - 1)));
			}
						
			qBuilder.append(" FROM " + object + " WHERE "
					+ criteria + " IN (");

			fieldsToAdd.clear();

			for (Map<String, String> recordsToField : recordToField) {
				for (Map.Entry<String, String> rec2field : recordsToField
						.entrySet()) {
					if (recordFieldObject.containsKey(rec2field.getKey())
							&& recordFieldObject.get(rec2field.getKey())
									.containsKey(rec2field.getValue())
							&& recordFieldObject.get(rec2field.getKey())
									.get(rec2field.getValue()).equals(object)) {
						recordsToAdd.add("'" + rec2field.getKey() + "',");
					}
				}
			}

			for (String record : recordsToAdd)
				qBuilder.append(record);

			recordsToAdd.clear();

			qBuilder.deleteCharAt(qBuilder.length() - 1);

			qBuilder.append(")");
			
			if(extraCriteriaToObj.containsKey(object))
			{
				qBuilder.append(" "+extraCriteriaToObj.get(object));
			}

			queries.add(qBuilder.toString());

			qBuilder = new StringBuilder();

		}

		return (queries);
	}

}