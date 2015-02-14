package com.dell.dfs.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;

public class PropertiesFactory implements IPropertiesFactory {

	@Override
	public Properties create(String filename) throws IOException, FileNotFoundException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(filename));
		return properties;
	}

	@Override
	public Properties create(Hashtable<String, Object> hashtable) {
		Properties properties = new Properties();

		for (String key : hashtable.keySet()) {
			properties.setProperty(key, String.valueOf(hashtable.get(key)));
		}

		return properties;
	}
}
