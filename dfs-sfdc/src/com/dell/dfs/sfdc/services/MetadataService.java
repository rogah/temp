package com.dell.dfs.sfdc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

public class MetadataService implements IMetadataService {

	private MetadataConnection _connection;
	
	public MetadataService(MetadataConnection connection) {
		_connection = connection;
	}
	
	@Override
	public List<DescribeMetadataObject> describeMetadata(String apiVersion) throws ConnectionException {
		
		DescribeMetadataResult result = _connection.describeMetadata(Double.valueOf(apiVersion));
		
		DescribeMetadataObject[] metadataObjects = result.getMetadataObjects();
		
		if (metadataObjects.length == 0)
			return new ArrayList<DescribeMetadataObject>();
		
		return Arrays.asList(metadataObjects);
	}
}
