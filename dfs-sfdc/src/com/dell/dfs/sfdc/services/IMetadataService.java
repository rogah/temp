package com.dell.dfs.sfdc.services;

import java.util.List;

import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.ws.ConnectionException;

public interface IMetadataService {

	List<DescribeMetadataObject> describeMetadata(String apiVersion) throws ConnectionException;
}
