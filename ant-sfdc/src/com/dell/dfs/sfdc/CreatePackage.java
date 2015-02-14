package com.dell.dfs.sfdc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.dell.dfs.properties.IPropertiesFactory;
import com.dell.dfs.properties.PropertiesFactory;
import com.dell.dfs.sfdc.factories.IConnectionFactory;
import com.dell.dfs.sfdc.factories.MetadataConnectionFactory;
import com.dell.dfs.sfdc.metadata.IManifestInfo;
import com.dell.dfs.sfdc.metadata.IManifestParser;
import com.dell.dfs.sfdc.metadata.ManifestParser;
import com.dell.dfs.sfdc.metadata.MetadataExtensionFilter;
import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.dell.dfs.sfdc.properties.SfdcProperties;
import com.dell.dfs.sfdc.services.IMetadataService;
import com.dell.dfs.sfdc.services.MetadataService;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.MetadataConnection;

public class CreatePackage extends Task implements Observer {

	private File _manifestFile;
	private File _sourceDirectory;
	private File _packageDirectory;
	
	public void setManifest(File manifestFile) {
		_manifestFile = manifestFile;
	}
	
	public void setSource(File sourceDirectory) {
		_sourceDirectory = sourceDirectory;
	}
	
	public void setPackage(File packageDirectory) {
		_packageDirectory = packageDirectory;
	}
	
	public void execute() throws BuildException {
		
		try {
			
			if (!isValid()) return;
			
			ISfdcProperties properties = getProperties();
			
			IConnectionFactory<MetadataConnection> connectionFactory = new MetadataConnectionFactory(properties);
			connectionFactory.addObserver(this);

			MetadataConnection connection = connectionFactory.createConnection();
			
			IMetadataService metadataService = new MetadataService(connection);
			List<DescribeMetadataObject> metadataObjects = metadataService.describeMetadata(properties.getApiVersion());
			
			IManifestParser parser = new ManifestParser(metadataObjects);
			
			IManifestInfo manifestInfo = parser.parse(new FileInputStream(_manifestFile));
			
			createPackage(manifestInfo);
			
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}
	
	@Override
	public void update(Observable observable, Object arg) {
		log(String.valueOf(arg));
	}

	private void createPackage(IManifestInfo manifestInfo) throws IOException {
		
		Path sourcePath = _sourceDirectory.toPath();
		Path packagePath = _packageDirectory.toPath();
		
		for (String typeName : manifestInfo.getTypeNames()) {
			
			if (!manifestInfo.hasMetadaDescription(typeName))
				continue;
		
			String directoryName = manifestInfo.getDirectoryName(typeName);
			Path packageTypePath = packagePath.resolve(Paths.get(directoryName));
			File sourceTypeDirectory = new File(_sourceDirectory, directoryName);
			
			if (!Files.exists(packageTypePath))
				Files.createDirectory(packageTypePath);
			
			if (manifestInfo.hasAllMembers(typeName)) {
				String fileExtension = manifestInfo.getFileExtension(typeName);
				copyFiles(sourcePath, packagePath, sourceTypeDirectory.listFiles(new MetadataExtensionFilter(fileExtension)));
			} else {
				copyFiles(sourcePath, packagePath, manifestInfo.getFiles(typeName));
			}
		}
		
		File packageXml = new File(_packageDirectory, "package.xml");
		Files.copy(_manifestFile.toPath(), packageXml.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		log(String.format("Packing : %s", packageXml.getName()));
	}

	private void copyFiles(Path sourcePath, Path packagePath, File[] files) throws IOException {
		
		for (File file : files) {
			
			Path relativePath = getRelativePath(sourcePath, file);
			Path originPath = sourcePath.resolve(relativePath);
			Path destinationPath = packagePath.resolve(relativePath);
			
			if (originPath.toFile().isDirectory()) {
				if (!Files.exists(destinationPath)) {
					log(String.format("Packing folder: %s", relativePath));
					Files.createDirectory(destinationPath);
				}
			} else {
				log(String.format("Packing: %s", relativePath));
				Files.copy(originPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
	
	private Path getRelativePath(final Path path, final File file) {
		if (file.isAbsolute())
			return path.relativize(file.toPath());
		
		return file.toPath();
	}
	
	private ISfdcProperties getProperties() {
		
		IPropertiesFactory factory = new PropertiesFactory();

		ISfdcProperties properies = new SfdcProperties(
			factory.create(getProject().getProperties())
		);
		
		return properies;
	}
	
	private boolean isValid() {
		
		if (!_manifestFile.exists())
			throw new BuildException(String.format("File does not exist: \"%s\"", _manifestFile.getAbsolutePath()));
		
		if (!_sourceDirectory.exists())
			throw new BuildException(String.format("Path does not exist: \"%s\"", _sourceDirectory.getAbsolutePath()));
		
		if (!_packageDirectory.exists())
			throw new BuildException(String.format("Path does not exist: \"%s\"", _packageDirectory.getAbsolutePath()));
		
		return true;
	}
}
