package com.dell.dfs.sfdc.metadata;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.Package;
import com.sforce.soap.metadata.PackageTypeMembers;

public class ManifestInfo implements IManifestInfo {

	private final Map<String, DescribeMetadataObject> _metadata = new HashMap<String, DescribeMetadataObject>();
	private final Map<String, List<String>> _types = new HashMap<String, List<String>>();
	private final String _apiVersion;
	
	public ManifestInfo(List<DescribeMetadataObject> metadataObjects, Package manifest) throws UnsupportedEncodingException {

		load(metadataObjects, manifest);
		
		_apiVersion = manifest.getVersion();
	}

	private void load(List<DescribeMetadataObject> metadataObjects, Package manifest) throws UnsupportedEncodingException {
		
		for (PackageTypeMembers packageType : manifest.getTypes()) {
			
			String typeName = packageType.getName();
			
			_types.put(typeName, Arrays.asList(packageType.getMembers()));
			
			for (DescribeMetadataObject metadataObject : metadataObjects) {
				
				if (typeName.equals(metadataObject.getXmlName())) {
					_metadata.put(typeName, metadataObject);
					break;
				}
			}
		}
	}
	
	private File getFile(File directory, String name, String extension) {
		if (StringUtils.isNotBlank(extension))
			return new File(directory, String.format("%s.%s", name, extension));
		return new File(directory, name);
	}
	
	private File getFile(File directory, String name) {
		return getFile(directory, name, null);
	}
	
	private File getMetadata(File directory, String name, String extension) {
		if (StringUtils.isNotBlank(extension))
			return new File(directory, String.format("%s.%s-meta.xml", name, extension));
		return new File(directory, String.format("%s-meta.xml", name, extension));
	}
	
	private File getMetadata(File directory, String name) {
		return getMetadata(directory, name, null);
	}
	
	@Override
	public String getApiVersion() {
		return _apiVersion;
	}

	@Override
	public Set<String> getTypeNames() {
		return _types.keySet();
	}

	@Override
	public List<String> getTypeMembers(String typeName) {
		return _types.get(typeName);
	}

	@Override
	public String getDirectoryName(String typeName) {
		if (!hasMetadaDescription(typeName))
			return null;
		return _metadata.get(typeName).getDirectoryName();
	}
	
	@Override
	public String getFileExtension(String typeName) {
		if (!hasMetadaDescription(typeName))
			return null;
		return _metadata.get(typeName).getSuffix();
	}

	@Override
	public File[] getFiles(String typeName) {
		
		File directory = new File(_metadata.get(typeName).getDirectoryName());
		
		DescribeMetadataObject describeMetadata = _metadata.get(typeName);
		
		boolean hasMetaFile = describeMetadata.getMetaFile();
		boolean inFolder = describeMetadata.getInFolder();
		String suffix = describeMetadata.getSuffix();
		
		List<String> members = _types.get(typeName);
		
		List<File> files = new ArrayList<File>();
		
		for (String member : members) {
			
			if (inFolder) {
				String folderName = member.substring(0, member.lastIndexOf("/"));
				File folder = getFile(directory, folderName);
				File folderMetadata = getMetadata(directory, folderName);
				
				if (!files.contains(folder))
					files.addAll(Arrays.asList(folder, folderMetadata));
			}
			
			files.add(getFile(directory, member, suffix));
			
			if (hasMetaFile)
				files.add(getMetadata(directory, member, suffix));
		}
		
		return files.toArray(new File[files.size()]);
	}

	@Override
	public boolean hasMetadaDescription(String typeName) {
		return _types.containsKey(typeName) && _metadata.containsKey(typeName);
	}

	@Override
	public boolean hasAllMembers(String typeName) {
		List<String> typeMembers = getTypeMembers(typeName);
		
		if (typeMembers == null)
			return false;
		
		if (typeMembers.contains("*"))
			return true;
		
		return false;
	}
}
