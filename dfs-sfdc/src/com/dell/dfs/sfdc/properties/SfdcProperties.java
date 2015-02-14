package com.dell.dfs.sfdc.properties;

import java.util.*;

public class SfdcProperties implements ISfdcProperties {

	public static final String API_VERSION = "31.0";

	public static Properties _properties;

	public SfdcProperties(Properties properties) {
		_properties = properties;
	}

	@Override
	public String getUsername() {
		String value = _properties.getProperty("sf.username");

		if (value != null)
			return value.trim();
		return value;
	}

	@Override
	public String getPassword() {
		String value = _properties.getProperty("sf.password");

		if (value != null)
			return value.trim();
		return value;
	}

	@Override
	public String getServerUrl() {
		String value = _properties.getProperty("sf.serverurl");

		if (value != null)
			return value.trim();
		return value;
	}

	@Override
	public String getProxyHost() {
		String value = _properties.getProperty("http.proxyHost");

		if (value != null)
			return value.trim();
		return value;
	}

	@Override
	public int getProxyPort() {
		String value = _properties.getProperty("http.proxyPort");

		if (value == null || value.trim().length() == 0)
			return 80;
		return Integer.parseInt(value);
	}

	@Override
	public boolean hasProxyHost() {
		return this.getProxyHost() != null;
	}

	@Override
	public String getApiVersion() {
		String value = _properties.getProperty("apiVersion");

		if (value != null)
			return value.trim();
		return API_VERSION;
	}
}
