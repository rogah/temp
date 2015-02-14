package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class WhenGettingTypeNames {

	private IManifestParser _parser;
	
	@Before
	public void setUp() {
		_parser = new ManifestParser(new ArrayList<DescribeMetadataObject>());
	}
	
	@Test
	public void shouldReturnSetOfTypeNames() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeNames(), instanceOf(Set.class));
	}

	@Test
	public void shouldReturnCorrectNumberOfTypeNames() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <name>ApexClass</name>" +
			"  </types>" +
			"  <types>" +
			"    <name>ApexTrigger</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeNames().size(), equalTo(2));
	}

	@Test
	public void shouldReturnCorrectTypeNames() throws Exception {
		
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
			"    <name>Profile</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		for (String typeName : manifestInfo.getTypeNames()) {
			assertThat(typeName, anyOf(equalTo("ApexClass"), equalTo("ApexTrigger"), equalTo("Profile")));
		}
	}
	
	@Test
	public void shouldReturnNoTypeWhenThereIsNone() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeNames().size(), equalTo(0));
	}
	
	@Test
	public void shouldReturn() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <name>CustomField</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeNames().size(), equalTo(1));
	}
}
