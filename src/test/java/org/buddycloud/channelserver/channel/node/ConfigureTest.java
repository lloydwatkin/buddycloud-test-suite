package org.buddycloud.channelserver.channel.node;

import java.util.HashMap;

import junit.framework.Assert;

import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.PacketReceivedQueue;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.junit.Ignore;

public class ConfigureTest extends ChannelServerTestHelper {

	@Test
	public void testNotProvidingNodeReturnsErrorStanza() throws Exception {

		TestPacket packet = getPacket("resources/channel/node/configure/missing-node-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply,
				"/iq/error[@type='MODIFY']/nodeid-required"));
	}

	@Test
	public void testSuccessfulConfigurationRetunsSuccessStanza()
			throws Exception {

		String node = createNode();
		String affiliation = "member";
		TestPacket packet = getPacket("resources/channel/node/configure/success.request");
		packet.setVariable("$NODE", node);
		packet.setVariable("$AFFILIATION", affiliation);
		packet.setVariable("$ACCESS_MODEL", "open");
		Packet reply = sendPacket(packet);
		
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));

		TestPacket checkRequest = getPacket("resources/channel/node/configure/retrieve.request");
		checkRequest.setVariable("$NODE", node);
		Packet configuration = sendPacket(checkRequest);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertEquals(
				"romeo@ip-10-66-2-93",
				getValue(
						configuration,
						"/iq/query[@node='"
								+ node
								+ "']/x[@type='result']/field[@var='pubsub#owner']/value/text()"));
		Assert.assertEquals(
				affiliation,
				getValue(
						configuration,
						"/iq/query[@node='"
								+ node
								+ "']/x[@type='result']/field[@var='buddycloud#default_affiliation']/value/text()"));
	}
	
	@Test
	public void testNotProvidingValidStanzaResultsInErrorResponse()
			throws Exception {
		String node = createNode();
		TestPacket packet = getPacket("resources/channel/node/configure/invalid.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet);
		
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
	}
	
	@Test
	public void testUserMustBeOwnerToModifyConfiguration() throws Exception {
		String node = createNode();
		
		TestPacket subscribe = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribe.setVariable("$NODE",  node);
		Packet subscribed = sendPacket(subscribe, 2);
		
		Assert.assertEquals("subscribed", getValue(subscribed, "/iq/pubsub/subscription/@subscription"));
		
		TestPacket packet = getPacket("resources/channel/node/configure/success.request", 2);
		packet.setVariable("$NODE", node);
		packet.setVariable("$AFFILIATION", "member");
		packet.setVariable("$ACCESS_MODEL", "open");
		Packet reply = sendPacket(packet, 2);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/forbidden"));
	}
	
	@Test
	public void testNotExistingNodeReturnsErrorStanza() throws Exception {

		TestPacket packet = getPacket("resources/channel/node/configure/not-existing-node.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='CANCEL']/item-not-found"));
	}
	
	
	@Test
	public void testSuccessfulConfigurtionSendsEventNotification() throws Exception
	{
		
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success.request");
		makeNodePrivate.setVariable("$AFFILIATION", "member");
		makeNodePrivate.setVariable("$ACCESS_MODEL", "authorize");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
		
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
					Assert.assertEquals(node, getValue(packet, "/message/event/configuration/@node"));
					return;
				}
			}
			Thread.sleep(40);
		}
		Assert.assertTrue("Did not receive notification message", false);
	}
}