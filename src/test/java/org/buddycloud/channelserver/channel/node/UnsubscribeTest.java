package org.buddycloud.channelserver.channel.node;

import java.util.HashMap;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.PacketReceivedQueue;
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

		Packet packet = getPacket("resources/channel/node/unsubscribe/missing-node-id.request");
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
	
	@Test
	public void testUnsubscribeSendsExceptedNotifications()
			throws Exception {
		
		String node = createNode();
		
		TestPacket subscribe = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribe.setVariable("$NODE", node);
		Packet reply = sendPacket(subscribe, 2);
		
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		// Require a sleep to wait until all previous notifications are received
		Thread.sleep(100);
		PacketReceivedQueue.clearPackets();
		
		TestPacket unsubscribe = getPacket("resources/channel/node/unsubscribe/success.request", 2);
		unsubscribe.setVariable("$NODE", node);
		sendPacket(unsubscribe, 2);
		
		HashMap<String, Packet> packets;
		
		// Try up to ten times to get the packet required, with a pause if required;
		for (int i = 0; i < 10; i++) {
			packets = PacketReceivedQueue.getPackets();
			
			for (Packet packet : packets.values()) {

				if (packet.getTo().contains((getUserJid(1)))) {
					Assert.assertTrue(exists(packet, "/message"));
					Assert.assertEquals("headline", getValue(packet, "/message/@type"));
					Assert.assertEquals("none", getValue(packet, "/message/event/subscription/@subscription"));
					Assert.assertEquals(node, getValue(packet, "/message/event/subscription/@node"));
					Assert.assertEquals(getUserJid(2), getValue(packet, "/message/event/subscription/@jid"));
					Assert.assertEquals("none", getValue(packet, "/message/event/affiliation/@affiliation"));
					Assert.assertEquals(node, getValue(packet, "/message/event/affiliation/@node"));
					Assert.assertEquals(getUserJid(2), getValue(packet, "/message/event/affiliation/@jid"));
					return;
				}
			}
			Thread.sleep(25);
		}
		Assert.assertTrue("Did not receive notification message", false);
	}
}