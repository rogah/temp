package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
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

public class WhenGettingTypeMembers {

	private IManifestParser _parser;
	
	@Before
	public void setUp() {
		_parser = new ManifestParser(new ArrayList<DescribeMetadataObject>());
	}

	@Test
	public void shouldReturnSetOfTypeMembers() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <name>ApexClass</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeMembers("ApexClass"), instanceOf(List.class));
	}

	@Test
	public void shouldReturnCorrectNumberOfTypeMembers() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <members>Foo</members>" +
			"    <members>Bar</members>" +
			"    <name>Skill</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeMembers("Skill").size(), equalTo(2));
	}

	@Test
	public void shouldReturnCorrectTypeMembers() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <members>Foo</members>" +
			"    <members>Bar</members>" +
			"    <members>Baz</members>" +
			"    <name>UserSharingRules</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		for (String typeMember : manifestInfo.getTypeMembers("UserSharingRules")) {
			assertThat(typeMember, anyOf(equalTo("Foo"), equalTo("Bar"), equalTo("Baz")));
		}
	}
	
	@Test
	public void shouldReturnReturnNoTypeMemberWhenThereIsNone() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <name>CustomObject</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeMembers("CustomObject").size(), equalTo(0));
	}
	
	@Test
	public void shouldReturnNullWhenTypeDoesNotExist() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <members>Foo</members>" +
			"    <name>ApexClass</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		assertThat(manifestInfo.getTypeMembers("CustomObject"), nullValue());
	}
	
	@Test
	public void shouldReturnAllWildCardCharacterWheverAstericsIsProvided() throws Exception {
		
		String packageXml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">" +
			"  <types>" +
			"    <members>*</members>" +
			"    <name>ApexClass</name>" +
			"  </types>" +
			"  <version>32.0</version>" + 
			"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		IManifestInfo manifestInfo = _parser.parse(inputStream);
		
		String allMembersWildcard = manifestInfo.getTypeMembers("ApexClass").get(0);
		
		assertThat(allMembersWildcard, equalTo("*"));
	}
}
