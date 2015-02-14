package com.dell.dfs.sfdc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.*;

public class SoapService extends Observable implements ISoapService {
	
	PartnerConnection _connection;

	public SoapService(PartnerConnection connection) {
		_connection = connection;
	}

	@Override
	public void create(SObject[] sObjects) throws ConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(SObject[] sObjects) throws ConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void upsert(String fieldNameOfExternalId, SObject[] sObjects)
			throws ConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String[] ids) throws ConnectionException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<SObject> query(String soql) throws ConnectionException {

		List<SObject> recordsList = new ArrayList<SObject>();
		
		QueryResult result = _connection.query(soql);

		if (result.getSize() == 0)
			return recordsList;
		
		boolean done = false;
		
		while (!done) {
			
			SObject[] records = result.getRecords();
			
			recordsList.addAll(Arrays.asList(records));

			if (result.isDone()) {
				done = true;
			} else {
				result = _connection.queryMore(result.getQueryLocator());
			}
		}
		
		notify("SOAP Query: %s. (%d)", soql, recordsList.size());
		
		return recordsList;
	}
	
	private void notify(String format, Object... args) {
		setChanged();
		notifyObservers(String.format(format, args)); 
	}
}