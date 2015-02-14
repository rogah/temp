package com.dell.dfs.sfdc.factories;

import java.util.Observable;

import com.dell.dfs.sfdc.properties.ISfdcProperties;
import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public abstract class ConnectionFactory<T> extends Observable implements IConnectionFactory<T> {

	private ISfdcProperties _properties;

	public ConnectionFactory(ISfdcProperties properties) {
		_properties = properties;
	}

	@Override
	public abstract T createConnection() throws ConnectionException, AsyncApiException;
	
	@Override
	public abstract T createConnection(String username, String password) throws ConnectionException, AsyncApiException;

	protected ISfdcProperties getProperties() {
		return _properties;
	}

	protected PartnerConnection createPartnerConnection(ConnectorConfig config) throws ConnectionException {		
		
		notify("Connecting: %s", config.getAuthEndpoint());		
		
		PartnerConnection connection = new PartnerConnection(config);		
		
		notify("Connected. Session Id: %s", config.getSessionId());

		return connection;
	}

	protected String getSoapAuthEndpoint() {
		return String.format("%s/services/Soap/u/%s", _properties.getServerUrl(), _properties.getApiVersion());
	}

	protected String getRestAuthEndpoint(String soapEndpoint) {
		return String.format("%sasync/%s", soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")), _properties.getApiVersion());
	}

	protected ConnectorConfig createPartnerConnectorConfig(String username, String password) {
		
		String endpoint = getSoapAuthEndpoint();

		notify("SOAP Endpoint: %s\nUsername: %s", endpoint, username); 

		ConnectorConfig config = createBaseConnectorConfig();
		config.setUsername(username);
		config.setPassword(password);
		config.setAuthEndpoint(endpoint);
		config.setServiceEndpoint(endpoint);

		return config;
	}

	protected ConnectorConfig createBulkConnectorConfig(String sessionId, String soapEndpoint) {

		String endpoint = getRestAuthEndpoint(soapEndpoint);

		notify("REST Endpoint: %s\nSession Id: %s", endpoint, sessionId); 

		ConnectorConfig config = createBaseConnectorConfig();
		config.setSessionId(sessionId);
		config.setRestEndpoint(endpoint);

		return config;
	}
	
	protected ConnectorConfig createMetadataConnectorConfig(String sessionId, String metadataServerUrl) {

		notify("Metadata Endpoint: %s\nSession Id: %s", metadataServerUrl, sessionId); 

		ConnectorConfig config = createBaseConnectorConfig();
		config.setSessionId(sessionId);
		config.setServiceEndpoint(metadataServerUrl);

		return config;
	}

	protected ConnectorConfig createBaseConnectorConfig() {
		
		ConnectorConfig config = new ConnectorConfig();
		config.setCompression(true);
		config.setTraceMessage(false);

		if (_properties.hasProxyHost()) {
			notify("HTTP Proxy: %s:%s", _properties.getProxyHost(), _properties.getProxyPort()); 
			config.setProxy(_properties.getProxyHost(), _properties.getProxyPort());
		}

		return config;
	}

	protected void notify(String format, Object... args) {
		setChanged();
		notifyObservers(String.format(format, args)); 
	}
}