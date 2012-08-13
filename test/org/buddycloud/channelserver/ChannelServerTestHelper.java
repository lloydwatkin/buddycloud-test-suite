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
import java.util.Properties;

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
		
		TestContext tc = new TestContext();
		
		tc.setTo(configuration.getProperty("channelserver_to_test"));
		tc.setTopicChannelServer(configuration.getProperty("topic_channel_server"));

		tc.setClientUser(configuration.getProperty("testclient_user"));
		tc.setClientPass(configuration.getProperty("testclient_pass"));
		
		tc.setClientResource(configuration.getProperty("testclient_resource"));
		tc.setServerHostname(configuration.getProperty("testclient_xmpphost"));
		tc.setServiceName(configuration.getProperty("testclient_xmppservice"));
		tc.setServerPort(Integer.parseInt(configuration
				.getProperty("testclient_xmppport")));
		
		long defaultPrefix = (long) (System.currentTimeMillis() / 1000L);
		tc.setResourcePrefix(
		    configuration.getProperty("channel_prefix", String.valueOf(defaultPrefix))
		);

		setContext(tc);
		initConnection();
	}
	
}
