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

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.jivesoftware.smack.packet.Packet;
import org.junit.Before;

/**
 * @author Abmar
 *
 */
public class ChannelServerTestHelper extends XMPPAcceptanceTestHelper {

	private static final String PROPERTIES_FILE = "suite.properties";
	
	@Before
	public void init() throws Exception {
		
		Properties configuration = new Properties();
		configuration.load(new FileReader(PROPERTIES_FILE));
		
		
		TestContext user1 = new TestContext();
		TestContext user2 = new TestContext();
		
		user1.setTo(configuration.getProperty("channelserver_to_test"));
		user1.setTopicChannelServer(configuration.getProperty("topic_channel_server"));
		user1.setServerHostname(configuration.getProperty("testclient_xmpphost"));
		user1.setServiceName(configuration.getProperty("testclient_xmppservice"));
		user1.setServerPort(Integer.parseInt(configuration
				.getProperty("testclient_xmppport")));
		
		user2.setTo(configuration.getProperty("channelserver_to_test"));
		user2.setTopicChannelServer(configuration.getProperty("topic_channel_server"));
		user2.setServerHostname(configuration.getProperty("testclient_xmpphost"));
		user2.setServiceName(configuration.getProperty("testclient_xmppservice"));
		user2.setServerPort(Integer.parseInt(configuration
				.getProperty("testclient_xmppport")));
		
		user1.setClientUser(configuration.getProperty("user1.username"));
		user1.setClientPass(configuration.getProperty("user1.password"));
		user1.setClientResource(configuration.getProperty("user1.resource"));
		
		user2.setClientUser(configuration.getProperty("user2.username"));
		user2.setClientPass(configuration.getProperty("user2.password"));
		user2.setClientResource(configuration.getProperty("user2.resource"));
				
		long defaultPrefix = (long) (System.currentTimeMillis() / 1000L);
		user1.setResourcePrefix(
		    configuration.getProperty("channel_prefix", String.valueOf(defaultPrefix))
		);

		setUsers(user1, user2);

		initConnection();
	}

	public String createNode() throws Exception {
		return createNode(1);
	}
	
	public String createNode(int userNumber) throws Exception {
		Packet packet = getPacket("resources/channel/node/create/success.request", userNumber);
		sendPacket(packet, userNumber);
		return getValue(packet, "/iq/pubsub/create/@node");
	}
	
	public void subscribeToNode(String node) throws Exception {
		subscribeToNode(node, 1);
	}
	
	public void subscribeToNode(String node, int userNumber) throws Exception {
		TestPacket packet = getPacket("resources/channel/node/subscribe/success.request", userNumber);
		packet.setVariable("$NODE", node);
		sendPacket(packet, userNumber); 
	}	
}
