package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class WhenCheckingWhetherHasAllMembersIncluded {

	private IManifestInfo _manifestInfo;
	
	@Before
	public void setUp() throws Exception {
		String packageXml = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
				"  <types>" +
				"    <members>Foo</members>" +
				"    <members>Bar</members>" +
				"    <name>ApexClass</name>" +
				"  </types>" +
				"  <types>" +
				"    <members>*</members>" +
				"    <name>CustomLabels</name>" +
				"  </types>" +
				"  <version>32.0</version>" + 
				"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestParser parser = new ManifestParser(new ArrayList<DescribeMetadataObject>());
		
		_manifestInfo = parser.parse(inputStream);
	}

	@Test
	public void shouldReturnTrueInCaseAllWildcardIsSpecified() throws Exception {
		assertThat(_manifestInfo.hasAllMembers("CustomLabels"), equalTo(true));
	}

	@Test
	public void shouldReturnFalseInCaseIndividualMembersAreSpecified() throws Exception {
		assertThat(_manifestInfo.hasAllMembers("ApexClass"), equalTo(false));
	}

	@Test
	public void shouldReturnFalseForTypeMemberNotSpecifiedInThePackage() throws Exception {
		assertThat(_manifestInfo.hasAllMembers("FooBar"), equalTo(false));
	}
}
