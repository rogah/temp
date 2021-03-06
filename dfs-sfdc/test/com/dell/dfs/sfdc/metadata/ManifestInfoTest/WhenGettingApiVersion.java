package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Test;

import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class WhenGettingApiVersion {
	
	@Test
	public void shouldReturnCorrectValue() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestParser parser = new ManifestParser(new ArrayList<DescribeMetadataObject>());
		
		IManifestInfo manifestInfo = parser.parse(inputStream);
		
		assertThat(manifestInfo.getApiVersion(), equalTo("32.0"));
	}
}
