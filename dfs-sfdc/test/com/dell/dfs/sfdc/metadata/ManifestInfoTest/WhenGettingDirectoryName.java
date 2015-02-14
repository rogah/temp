package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class WhenGettingDirectoryName {

	private IManifestInfo _manifestInfo;
	
	@Before
	public void setUp() throws Exception {
		
		String packageXml = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
				"  <types>" +
				"    <name>ApexClass</name>" +
				"  </types>" +
				"  <types>" +
				"    <name>CustomLabels</name>" +
				"  </types>" +
				"  <types>" +
				"    <name>CustomField</name>" +
				"  </types>" +
				"  <version>32.0</version>" + 
				"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		List<DescribeMetadataObject> metadataObjects = new ArrayList<DescribeMetadataObject>();
		
		DescribeMetadataObject apexClass = new DescribeMetadataObject();
		apexClass.setXmlName("ApexClass");
		apexClass.setDirectoryName("classes");
		metadataObjects.add(apexClass);
		
		DescribeMetadataObject apexTrigger = new DescribeMetadataObject();
		apexTrigger.setXmlName("ApexTrigger");
		apexTrigger.setDirectoryName("triggers");
		metadataObjects.add(apexTrigger);
		
		DescribeMetadataObject customLabels = new DescribeMetadataObject();
		customLabels.setXmlName("CustomLabels");
		customLabels.setDirectoryName("labels");
		metadataObjects.add(customLabels);
		
		IManifestParser parser = new ManifestParser(metadataObjects);
		
		_manifestInfo = parser.parse(inputStream);
	}

	@Test
	public void shouldReturnCorrectFolderName() {
		
		String directoryName = _manifestInfo.getDirectoryName("ApexClass");
		
		assertThat(directoryName, equalTo("classes"));
	}

	@Test
	public void shouldReturnNullIfTypeDoesNotHaveFolder() {
		
		String directoryName = _manifestInfo.getDirectoryName("CustomField");
		
		assertThat(directoryName, nullValue());
	}
}
