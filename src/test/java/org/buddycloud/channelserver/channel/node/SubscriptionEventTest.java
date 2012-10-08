package org.buddycloud.channelserver.channel.node;

import java.util.HashMap;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.PacketReceivedQueue;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;

public class SubscriptionEventTest extends ChannelServerTestHelper {
	
	private static final Logger LOGGER = Logger.getLogger(SubscriptionEventTest.class);
	
	@Test
	public void testSuccesfulSubscriptionSendsExceptedNotifications()
			throws Exception {
		
		String node = createNode();
		
		PacketReceivedQueue.clearPackets();
		
		TestPacket subscribe = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribe.setVariable("$NODE", node);
		Packet reply = sendPacket(subscribe, 2);
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));

		HashMap<String, Packet> packets;
		
		// Try up to five times to get the packet required, with a pause if required;
		for (int i = 0; i < 5; i++) {
			packets = PacketReceivedQueue.getPackets();
			
			for (Packet packet : packets.values()) {

				if (packet.getTo().contains((getUserJid(1)))) {
					Assert.assertTrue(exists(packet, "/message"));
					Assert.assertEquals("headline", getValue(packet, "/message/@type"));
					Assert.assertEquals("subscribed", getValue(packet, "/message/event/subscription/@subscription"));
					Assert.assertEquals(node, getValue(packet, "/message/event/subscription/@node"));
					Assert.assertEquals(getUserJid(2), getValue(packet, "/message/event/subscription/@jid"));
					Assert.assertEquals("publisher", getValue(packet, "/message/event/affiliation/@affiliation"));
					Assert.assertEquals(node, getValue(packet, "/message/event/affiliation/@node"));
					Assert.assertEquals(getUserJid(2), getValue(packet, "/message/event/affiliation/@jid"));
					return;
				}
			}
			Thread.sleep(40);
		}
		Assert.assertTrue("Did not receive notification message", false);
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
