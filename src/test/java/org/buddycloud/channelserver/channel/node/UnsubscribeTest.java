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
public class UnsubscribeTest extends ChannelServerTestHelper {
	private static final Logger LOGGER = Logger.getLogger(UnsubscribeTest.class);

	@Test
	public void testNotProvidingNodeReturnsErrorStanza() throws Exception {

		Packet packet = getPacket("resources/channel/node/unsubscribe/missing-nodeid.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='MODIFY']/nodeid-required"));
	}

	@Test
	public void testTryingToUnsubscribeToNonExistentNodeReturnsErrorStanza()
			throws Exception {

		Packet packet = getPacket("resources/channel/node/unsubscribe/not-existing-node.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='CANCEL']/item-not-found"));
	}

	@Test
	public void testSuccesfulUnsubscribeReturnsExceptedResponse()
			throws Exception {
		String node = createNode();
		TestPacket packet = getPacket("resources/channel/node/unsubscribe/success.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
	}

	@Test
	public void testAttemptingToUnsubscribeWhenNotSubscribedReturnsErrorStanza()
			throws Exception {
		
		// We just accept multiple unsubscribes - for now?
		String node = createNode();
		TestPacket packet = getPacket("resources/channel/node/unsubscribe/success.request");
		packet.setVariable("$NODE", node);
		sendPacket(packet);

		TestPacket UnsubscribeAgainPacket = getPacket("resources/channel/node/unsubscribe/success.request");
		UnsubscribeAgainPacket.setVariable("$NODE", node);
		Packet reply = sendPacket(UnsubscribeAgainPacket);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
	}
}