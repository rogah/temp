package com.dell.dfs.sfdc.services;

import java.util.List;

import com.dell.dfs.core.IObservable;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public interface ISoapService extends IObservable {
	void create(SObject[] sObjects) throws ConnectionException;
	void update(SObject[] sObjects) throws ConnectionException;
	void upsert(String fieldNameOfExternalId, SObject[] sObjects) throws ConnectionException;
	void delete(String[] ids) throws ConnectionException;
	List<SObject> query(String soql) throws ConnectionException;
}