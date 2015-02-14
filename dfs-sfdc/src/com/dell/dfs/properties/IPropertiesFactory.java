package com.dell.dfs.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;

public interface IPropertiesFactory {
	Properties create(String filename) throws IOException, FileNotFoundException;
	Properties create(Hashtable<String, Object> hashtable);
}