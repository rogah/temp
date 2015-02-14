package com.dell.dfs.sfdc.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.Package;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.PullParserException;
import com.sforce.ws.parser.XmlInputStream;

public class ManifestParser implements IManifestParser {

	private List<DescribeMetadataObject> _metadataObjects;
	
	public ManifestParser(List<DescribeMetadataObject> metadataObjects) {
		_metadataObjects = metadataObjects;
	}

	@Override
	public IManifestInfo parse(InputStream in) throws PullParserException, IOException, ConnectionException {
		
		XmlInputStream xis = new XmlInputStream();
		xis.setInput(in, "UTF-8");
		
		Package manifest = new Package();
		manifest.load(xis, new TypeMapper());

		return new ManifestInfo(_metadataObjects, manifest);
	}

}
