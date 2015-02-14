package com.dell.dfs.sfdc.metadata;

import java.io.IOException;
import java.io.InputStream;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.parser.PullParserException;

public interface IManifestParser {

	IManifestInfo parse(InputStream inputStream) throws PullParserException, IOException, ConnectionException;
}
