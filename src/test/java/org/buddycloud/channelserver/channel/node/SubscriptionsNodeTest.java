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

	private static final Logger LOGGER = Logger
			.getLogger(SubscriptionsNodeTest.class);

	@Test
	@Ignore("Return to this to handle namespaces")
	public void testSubscriptionsNodeReturnsDataInExpectedFormat()
			throws Exception {

		Packet packet = getPacket(
				"resources/channel/node/subscription-item-retrieval/success.request",
				2);
		Packet reply = sendPacket(packet, 2);

		// Get node name
		System.out.println("\n\n" + packet.toXML());
		System.out.println("\n\n" + reply.toXML() + "\n\n");

		String node = getValue(reply, "/iq/pubsub/items/@node", true);

		Assert.assertEquals("result", getValue(reply, "/iq/@type", true));

        Assert.assertTrue(exists(reply, "/iq/pubsub/items/item[@id='" + getUserJid(2) + "']/query/item/node", true));
        Assert.assertEquals("owner", getValue(reply, "/iq/pubsub/items/item[@id='" + getUserJid(2) + "']/query/item/ns1:affiliation", true));
        Assert.assertEquals("subscribed", getValue(reply, "/iq/pubsub/items/item[@id='" + getUserJid(2) + "']/query/item/ns2:subscription", true));
        Assert.assertEquals(getUserJid(2), getValue(reply, "/iq/pubsub/items/item[@id='" + getUserJid(2) + "']/query/item/jid", true));
	}
}