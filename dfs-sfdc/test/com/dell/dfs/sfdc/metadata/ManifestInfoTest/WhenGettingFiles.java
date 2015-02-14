package com.dell.dfs.sfdc.metadata.ManifestInfoTest;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class WhenGettingFiles {

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
				"    <members>Baz</members>" +
				"    <name>CustomLabels</name>" +
				"  </types>" +
				"  <types>" +
				"    <members>Qux/Foo</members>" +
				"    <members>Qux/Bar</members>" +
				"    <name>EmailTemplate</name>" +
				"  </types>" +
				"  <types>" +
				"    <members>Thud/Baz.png.png</members>" +
				"    <members>Thud/Qux.jpg</members>" +
				"    <name>Document</name>" +
				"  </types>" +
				"  <version>32.0</version>" + 
				"</Package>";
		
		InputStream inputStream = new ByteArrayInputStream(packageXml.getBytes());
		
		List<DescribeMetadataObject> metadataObjects = new ArrayList<DescribeMetadataObject>();
		
		DescribeMetadataObject apexClass = new DescribeMetadataObject();
		apexClass.setXmlName("ApexClass");
		apexClass.setDirectoryName("classes");
		apexClass.setSuffix("cls");
		apexClass.setMetaFile(true);
		apexClass.setInFolder(false);
		apexClass.setChildXmlNames(new String[] {});
		metadataObjects.add(apexClass);
		
		DescribeMetadataObject apexTrigger = new DescribeMetadataObject();
		apexTrigger.setXmlName("ApexTrigger");
		apexTrigger.setDirectoryName("triggers");
		apexTrigger.setSuffix("trigger");
		apexTrigger.setMetaFile(true);
		apexTrigger.setInFolder(false);
		apexTrigger.setChildXmlNames(new String[] {});
		metadataObjects.add(apexTrigger);
		
		DescribeMetadataObject customLabels = new DescribeMetadataObject();
		customLabels.setXmlName("CustomLabels");
		customLabels.setDirectoryName("labels");
		customLabels.setSuffix("labels");
		customLabels.setMetaFile(false);
		customLabels.setInFolder(false);
		customLabels.setChildXmlNames(new String[] { "CustomLabel" });
		metadataObjects.add(customLabels);
		
		DescribeMetadataObject emailTemplate = new DescribeMetadataObject();
		emailTemplate.setXmlName("EmailTemplate");
		emailTemplate.setDirectoryName("email");
		emailTemplate.setSuffix("email");
		emailTemplate.setMetaFile(true);
		emailTemplate.setInFolder(true);
		emailTemplate.setChildXmlNames(new String[] {});
		metadataObjects.add(emailTemplate);
		
		DescribeMetadataObject documents = new DescribeMetadataObject();
		documents.setXmlName("Document");
		documents.setDirectoryName("documents");
		documents.setSuffix(null);
		documents.setMetaFile(true);
		documents.setInFolder(true);
		documents.setChildXmlNames(new String[] {});
		metadataObjects.add(documents);
				
		IManifestParser parser = new ManifestParser(metadataObjects);
		
		_manifestInfo = parser.parse(inputStream);
	}

	@Test
	public void shouldReturnCorrectFilesIncludingMetaXmlFiles() {
		
		File[] fileNames = _manifestInfo.getFiles("ApexClass");
		
		for (File file : fileNames) {
			assertThat(file.getPath(), anyOf(
					equalTo("classes\\Foo.cls"),
					equalTo("classes\\Foo.cls-meta.xml"),
					equalTo("classes\\Bar.cls"),
					equalTo("classes\\Bar.cls-meta.xml")));
		}
	}

	@Test
	public void shouldNotReturnMetaXmlFileForTypesThatAreNotSupposedTo() {
		
		File[] fileNames = _manifestInfo.getFiles("CustomLabels");
		
		for (File file : fileNames) {
			assertThat(file.getPath(), equalTo("labels\\Baz.labels"));
		}
	}

	@Test
	public void shouldReturnSubFolderAsPathAndIncludedInFileNameIfInFolderIsTrue() {
		
		List<String> expectedFileNames = Arrays.asList(
				"email\\Qux", 
				"email\\Qux-meta.xml", 
				"email\\Qux\\Foo.email",
				"email\\Qux\\Foo.email-meta.xml",
				"email\\Qux\\Bar.email",
				"email\\Qux\\Bar.email-meta.xml"
		);
		
		File[] files = _manifestInfo.getFiles("EmailTemplate");
		
		List<String> fileNames = new ArrayList<String>();
		
		for (File file : files) {
			fileNames.add(file.getPath());
		}
		
		assertEquals(expectedFileNames, fileNames);
	}
	
	@Test
	public void shouldReturnDocumentsWithCorrectFileExtentionAndMetaInformation() {
		
		List<String> expectedFileNames = Arrays.asList(
				"documents\\Thud", 
				"documents\\Thud-meta.xml", 
				"documents\\Thud\\Baz.png.png",
				"documents\\Thud\\Baz.png.png-meta.xml",
				"documents\\Thud\\Qux.jpg",
				"documents\\Thud\\Qux.jpg-meta.xml"
		);
		
		File[] files = _manifestInfo.getFiles("Document");
		
		List<String> fileNames = new ArrayList<String>();
		
		for (File file : files) {
			fileNames.add(file.getPath());
		}
		
		assertEquals(expectedFileNames, fileNames);
	}
}
