package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.junit.Ignore;

public class CreateTest extends ChannelServerTestHelper {
	
	@Test
	public void testNotProvidingNodeReturnsErrorStanza() throws Exception {
		
		TestPacket packet = getPacket("resources/channel/node/create/missing-node-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		/**
		 * XPath should be: /iq/error[@type='MODIFY']/bad-request[@xmlns='urn:ietf:params:xml:ns:xmpp-stanzas']/
		 * But the XMLNS attribute causes issues so skipping for now
		 */
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
		// /iq/error[@type='MODIFY']/nodeid-required[@xmlns='http://jabber.org/protocol/pubsub#errors']
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/nodeid-required"));
	}
	
	@Test
	public void testCanCreateNode() throws Exception {
		TestPacket packet = getPacket("resources/channel/node/create/success.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
	}

	@Test
	public void testCreatingNodeNotHandledReturnsErrorStanza() throws Exception {
		TestPacket packet = getPacket("resources/channel/node/create/not-existing-server.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		/**
		 * XPath should be: /iq/error[@type='MODIFY']/bad-request[@xmlns='urn:ietf:params:xml:ns:xmpp-stanzas']/
		 * But the XMLNS attribute causes issues so skipping for now
		 */
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/not-acceptable"));
	}
	
	@Test
	public void testAttemptingToCreateDuplicateNodeReturnsErrorStanza() throws Exception {
		
		String originalPacketXml = getPacketXml("resources/channel/node/create/success.request");
		String errorPacketXml = getPacketXml("resources/channel/node/create/success.request");
		
		TestPacket originalPacket = preparePacket(originalPacketXml);
		TestPacket errorPacket = preparePacket(errorPacketXml);
		
		Packet originalReply = sendPacket(originalPacket);
		Packet errorReply = sendPacket(errorPacket);

		
		Assert.assertEquals("result", getValue(originalReply, "/iq/@type"));
		Assert.assertEquals("error", getValue(errorReply, "/iq/@type"));

		Assert.assertTrue(exists(errorReply, "/iq/error[@type='CANCEL']/conflict"));
	}
}