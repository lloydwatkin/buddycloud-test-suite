package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 * 
 */
public class SubscriptionsNodeTest extends ChannelServerTestHelper {
	
	private static final Logger LOGGER = Logger.getLogger(SubscriptionsNodeTest.class);

	@Test
	@Ignore("Need to solve the XML parsing issue")
	public void testNotHavingAnySubscriptionsReturnsAsExpected() throws Exception {

		Packet packet = getPacket("resources/channel/node/subscription-item-retrieval/success.request", 2);
		Packet reply = sendPacket(packet, 2);
		
		// Get node name
		System.out.println(reply.toXML());
		String node = getValue(reply, "/iq/pubsub/items/@node");

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
	}
}