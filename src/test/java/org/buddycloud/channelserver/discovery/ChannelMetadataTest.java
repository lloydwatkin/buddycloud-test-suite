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
package org.buddycloud.channelserver.discovery;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Abmar
 *
 */
public class ChannelMetadataTest extends ChannelServerTestHelper {

	public static final Logger LOGGER = Logger.getLogger(ChannelMetadataTest.class);
	
	@Test
	@Ignore("Not supported by server yet")
	public void testMetadata() throws Exception {
		
		Packet packet = getPacket("resources/channel/node/discovery-item.request");
		System.out.println("Outgoing message: " + packet.toXML());
		Packet reply = sendPacket(packet);


		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, 
				"/iq/query/identity[@type='channel' and @category='pubsub']"));
		
		Assert.assertEquals("http://jabber.org/protocol/pubsub#meta-data", 
				getText(reply, "/iq/query/x/field[@var='FORM_TYPE']/value/text()"));
		
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='pubsub#title']"));
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='pubsub#description']"));
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='pubsub#access_model']"));
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='pubsub#publish_model']"));
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='buddycloud#default_affiliation']"));
		Assert.assertTrue(exists(reply, "/iq/query/x/field[@var='pubsub#creation_date']"));
		
	}

}
