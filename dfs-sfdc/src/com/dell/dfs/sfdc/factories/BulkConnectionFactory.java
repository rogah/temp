package com.dell.dfs.sfdc.factories;

import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class BulkConnectionFactory extends ConnectionFactory<BulkConnection> {

	public BulkConnectionFactory(ISfdcProperties properties) {
		super(properties);
	}

	@Override
	public BulkConnection createConnection() throws ConnectionException, AsyncApiException {
		return createConnection(
			this.getProperties().getUsername(), 
			this.getProperties().getPassword());
	}

	@Override
	public BulkConnection createConnection(String username, String password) throws ConnectionException, AsyncApiException {
		ConnectorConfig partnerConfig = this.createPartnerConnectorConfig(username, password);

		this.createPartnerConnection(partnerConfig);
		
		ConnectorConfig bulkConfig = this.createBulkConnectorConfig(
			partnerConfig.getSessionId(), 
			partnerConfig.getServiceEndpoint());
        
        return new BulkConnection(bulkConfig);
	}
}