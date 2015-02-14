package com.dell.dfs.sfdc.factories;

import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class MetadataConnectionFactory extends ConnectionFactory<MetadataConnection> {

	public MetadataConnectionFactory(ISfdcProperties properties) {
		super(properties);
	}

	@Override
	public MetadataConnection createConnection() throws ConnectionException, AsyncApiException {
		return createConnection(
			this.getProperties().getUsername(), 
			this.getProperties().getPassword());
	}

	@Override
	public MetadataConnection createConnection(String username, String password) throws ConnectionException, AsyncApiException {
		ConnectorConfig partnerConfig = this.createPartnerConnectorConfig(username, password);
		partnerConfig.setManualLogin(true);

		PartnerConnection partnerConnection = new PartnerConnection(partnerConfig);
		
		LoginResult loginResult = partnerConnection.login(username, password);
		
		ConnectorConfig metadataConfig = this.createMetadataConnectorConfig(
				loginResult.getSessionId(), 
				loginResult.getMetadataServerUrl());
        
        return new MetadataConnection(metadataConfig);
	}

}
