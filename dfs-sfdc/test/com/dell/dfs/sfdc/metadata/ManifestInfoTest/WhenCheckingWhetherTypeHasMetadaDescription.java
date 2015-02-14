package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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

public class WhenCheckingWhetherTypeHasMetadaDescription {

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
				"    <name>ApexTrigger</name>" +
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
	public void shouldReturnTrueInCaseTypeHasMetadaDescription() {
		
		boolean hasMetadaDescription = _manifestInfo.hasMetadaDescription("ApexTrigger");
		
		assertThat(hasMetadaDescription, equalTo(true));
	}

	@Test
	public void shouldReturnFalseInCaseTypeDoesNotHaveMetadaDescription() {
		
		boolean hasMetadaDescription = _manifestInfo.hasMetadaDescription("CustomField");
		
		assertThat(hasMetadaDescription, equalTo(false));
	}
}
