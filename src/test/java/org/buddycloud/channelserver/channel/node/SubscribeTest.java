package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 * 
 */
public class SubscribeTest extends ChannelServerTestHelper {
	private static final Logger LOGGER = Logger.getLogger(SubscribeTest.class);

	@Test
	public void testNotProvidingNodeReturnsErrorStanza() throws Exception {

		Packet packet = getPacket("resources/channel/node/subscribe/missing-node-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/nodeid-required"));
	}

	@Test
	public void testPassingAnInvalidJidReturnsErrorStanza() throws Exception {

		Packet packet = getPacket("resources/channel/node/subscribe/bad-jid.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/invalid-jid"));
	}

	@Test
	public void testTryingToSubscribeToNonExistentNodeReturnsErrorStanza()
			throws Exception {

		Packet packet = getPacket("resources/channel/node/subscribe/not-existing-node.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='CANCEL']/item-not-found"));
	}

	@Test
	public void testSuccesfulSubscriptionReturnsExceptedResponse()
			throws Exception {
		String node = createNode();
		
		TestPacket unsubscribePacket = getPacket("resources/channel/node/unsubscribe/success.request");
		unsubscribePacket.setVariable("$NODE", node);
		sendPacket(unsubscribePacket);
		
		TestPacket packet = getPacket("resources/channel/node/subscribe/success.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertEquals("subscribed",
				getValue(reply, "/iq/pubsub/subscription/@subscription"));
		Assert.assertEquals("publisher",
				getValue(reply, "/iq/pubsub/affiliation/@affiliation"));

	}

	@Test
	@Ignore("Require other functionality first")
	public void testOutcastCanNotSubscribe() throws Exception {
	}

	@Test
	public void testAttemptingToSubscribeWhenSubscribedReturnsErrorStanza()
			throws Exception {
		String node = createNode();
		TestPacket packet = getPacket("resources/channel/node/subscribe/success.request");
		packet.setVariable("$NODE", node);
		sendPacket(packet);

		TestPacket subscribeAgainPacket = getPacket("resources/channel/node/subscribe/success.request");
		subscribeAgainPacket.setVariable("$NODE", node);
		Packet reply = sendPacket(subscribeAgainPacket);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='WAIT']/policy-violation"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='WAIT']/too-many-subscriptions"));
	}
}