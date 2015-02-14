package com.dell.dfs.sfdc;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class BulkDml extends Task implements Observer {

	private String _sObject;
	private File _file;
	private File _transformationSpec;
	private boolean _failOnFileNotFound;
	
	public void setsObject(String sObject) {
        _sObject = sObject;
    }
	
	public String getsObject() {
        return _sObject;
    }
	
	public void setFile(File file) {
        _file = file;
    }
	
	public File getFile() {
		return _file;
	}

	public void setTransformationSpec(File transformationSpec) {
        _transformationSpec = transformationSpec;
    }
	
	public File getTransformationSpec() {
        return _transformationSpec;
    }
	
	public void setFailOnFileNotFound(boolean failOnFileNotFound) {
        _failOnFileNotFound = failOnFileNotFound;
    }
	
	public boolean getFailOnFileNotFound() {
        return _failOnFileNotFound;
    }
	
	public abstract void execute() throws BuildException;
	
	protected abstract boolean isValid();
	
	@Override
	public void update(Observable observable, Object arg) {
    	log(String.valueOf(arg));
    }
}
