/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.buddycloud.channelserver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abmar
 * 
 */
public class TestContext {

	private String clientUser;
	private String clientPass;
	private String clientResource;

	private String serverHostname;
	private int serverPort;
	private String serviceName;

	private String to;
	private String topicChannelServer;
	
	private String resourcePrefix;

	/**
	 * @return the clientUser
	 */
	public String getClientUser() {
		return clientUser;
	}

	/**
	 * @param clientUser
	 *            the clientUser to set
	 */
	public void setClientUser(String clientUser) {
		this.clientUser = clientUser;
	}

	/**
	 * @return the clientPass
	 */
	public String getClientPass() {
		return clientPass;
	}

	/**
	 * @param clientPass
	 *            the clientPass to set
	 */
	public void setClientPass(String clientPass) {
		this.clientPass = clientPass;
	}

	/**
	 * @return the serverHostname
	 */
	public String getServerHostname() {
		return serverHostname;
	}

	/**
	 * @param serverHostname
	 *            the serverHostname to set
	 */
	public void setServerHostname(String serverHostname) {
		this.serverHostname = serverHostname;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @return the channelServerToTest
	 */
	public String getTo() {
		return to;
	}
	
	/**
	 * Get topic channel server
	 */
	public String getTopicChannelServer() {
		return this.topicChannelServer;
	}
	
	/**
	 * Set topicChannelServer domain
	 */
	public void setTopicChannelServer(String topicChannelServer) {
		this.topicChannelServer = topicChannelServer;
	}

	/**
	 * @param to
	 *            the channelServerToTest to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	public String getClientResource() {
		return clientResource;
	}

	public void setClientResource(String clientResource) {
		this.clientResource = clientResource;
	}
	
	public void setResourcePrefix(String prefix)
	{
	    this.resourcePrefix = prefix;	
	}
	
	public String getResourcePrefix()
	{
		return this.resourcePrefix;
	}
	
	/**
	 * @return 
	 * 
	 */
	public Map<String, String> toMap() {
		Map<String, String> properties = new HashMap<String, String>();
		
		properties.put("$USER_JID", clientUser + "@" + serviceName + "/" + clientResource);
		properties.put("$USER_JID_BARE", clientUser + "@" + serviceName);
		properties.put("$USER_SERVICENAME", serviceName);
		properties.put("$USER_RESOURCE", clientResource);
		properties.put("$USER_NAME", clientUser);
		properties.put("$CHANNEL_SERVER", to);
		properties.put("$TOPIC_CHANNEL", topicChannelServer);
		properties.put("$RESOURCE_PREFIX", resourcePrefix);
		
		return properties;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

}
