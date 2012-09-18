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


import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Abmar
 *
 */
public class FirehoseTest extends ChannelServerTestHelper {

	@Test
	@Ignore("Doesn't appear to be ready yet")
	public void testFirehose() throws Exception {
		
		TestPacket packet = getPacket("resources/discovery/discovery-node.request");
		packet.setVariable("$NODE", "/firehose");
		
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, 
				"/iq/query/identity[@type='channel' and @category='pubsub']"));
		
	}


}
