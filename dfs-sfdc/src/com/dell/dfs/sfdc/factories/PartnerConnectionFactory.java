package com.dell.dfs.sfdc.factories;

import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class PartnerConnectionFactory extends ConnectionFactory<PartnerConnection> {

	public PartnerConnectionFactory(ISfdcProperties properties) {
		super(properties);
	}

	@Override
	public PartnerConnection createConnection() throws ConnectionException {
		return createConnection(
			getProperties().getUsername(), 
			getProperties().getPassword());
	}

	@Override
	public PartnerConnection createConnection(String username, String password) throws ConnectionException {		
		ConnectorConfig partnerConfig = createPartnerConnectorConfig(username, password);
		return createPartnerConnection(partnerConfig);
	}
}