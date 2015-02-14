package com.dell.dfs.sfdc.properties;

public interface ISfdcProperties {

	String getUsername();

	String getPassword();

	String getServerUrl();

	String getProxyHost();

	int getProxyPort();
	
	boolean hasProxyHost();

	String getApiVersion();
}
