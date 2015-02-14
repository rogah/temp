package com.dell.dfs.sfdc.factories;

import com.dell.dfs.core.IObservable;
import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

public interface IConnectionFactory<T> extends IObservable {
	T createConnection() throws ConnectionException, AsyncApiException;
	T createConnection(String username, String password) throws ConnectionException, AsyncApiException;
}