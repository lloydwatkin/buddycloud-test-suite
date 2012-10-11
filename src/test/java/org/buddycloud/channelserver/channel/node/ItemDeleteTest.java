package org.buddycloud.channelserver.channel.node;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.PacketReceivedQueue;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 * 
 */
public class ItemDeleteTest extends ChannelServerTestHelper {
	private static final Logger logger = Logger.getLogger(ItemDeleteTest.class);

	@Test
	public void testNotPassingNodeReturnsExpectedError() throws Exception {

		TestPacket packet = getPacket("resources/channel/node/item-delete/missing-node-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		/**
		 * XPath should be: /iq/error[@type=
		 * 'MODIFY']/bad-request[@xmlns='urn:ietf:params:xml:ns:xmpp-stanzas']/
		 * But the XMLNS attribute causes issues so skipping for now
		 */
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
		// /iq/error[@type='MODIFY']/nodeid-required[@xmlns='http://jabber.org/protocol/pubsub#errors']
		Assert.assertTrue(exists(reply,
				"/iq/error[@type='MODIFY']/nodeid-required"));
	}

	@Test
	public void testNotExistingNodeIdReturnsExpectedError() throws Exception {
		TestPacket packet = getPacket("resources/channel/node/item-delete/not-existing-node.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq/error[@type='CANCEL']/item-not-found"));
	}

	@Test
	public void testNotProvidingAnItemIdReturnsErrorStanza() throws Exception {
		TestPacket packet = getPacket("resources/channel/node/item-delete/missing-item-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply,
				"/iq/error[@type='MODIFY']/item-required"));
	}

	@Test
	public void testItemDoesNotExistReturnsError() throws Exception {
		String itemDeleteXml = getPacketXml("resources/channel/node/item-delete/success.request");
		TestPacket itemDelete = preparePacket(itemDeleteXml.replace("$NODE",
				"/user/$USER_NAME@$USER_SERVICENAME/posts"));
		itemDelete.setVariable("$ITEM_ID", "item-id");
		Packet reply = sendPacket(itemDelete);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq/error[@type='CANCEL']/item-not-found"));
	}

	@Test
	public void testUserWhoIsNotItemOwnerOrNodeOwnerCanNotDeleteItem()
			throws Exception {

		String makePostXml = getPacketXml("resources/channel/node/item-post/create.request");
		TestPacket makePost = preparePacket(makePostXml.replace("$NODE",
				"/user/$USER_NAME@$USER_SERVICENAME/posts"));
		Packet postedItem = sendPacket(makePost);
		Assert.assertTrue(exists(postedItem, "/iq/pubsub/publish/item/@id"));
		String itemId = getValue(postedItem, "/iq/pubsub/publish/item/@id");

		String itemDeleteXml = getPacketXml("resources/channel/node/item-delete/success.request");
		TestPacket itemDelete = preparePacket(
				itemDeleteXml.replace("$NODE", "/user/" + getUserJid(1)
						+ "/posts"), 2);
		itemDelete.setVariable("$ITEM_ID", itemId);

		Packet reply = sendPacket(itemDelete, 2);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/forbidden"));
	}

	@Test
	public void testDeletingItemReturnsResultStanza() throws Exception {

		String node = "/user/" + getUserJid(1) + "/posts";

		TestPacket makePost = getPacket("resources/channel/node/item-post/create.request");
		makePost.setVariable("$NODE", node);
		Packet postedItem = sendPacket(makePost);
		Assert.assertTrue(exists(postedItem, "/iq/pubsub/publish/item/@id"));
		String itemId = getValue(postedItem, "/iq/pubsub/publish/item/@id");

		String itemDeleteXml = getPacketXml("resources/channel/node/item-delete/success.request");

		TestPacket itemDelete = preparePacket(itemDeleteXml.replace(
				"notify=\'true\'", ""));
		itemDelete.setVariable("$NODE", node);
		itemDelete.setVariable("$ITEM_ID", itemId);

		PacketReceivedQueue.clearPackets();

		Packet reply = sendPacket(itemDelete);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		HashMap<String, Packet> packets;
		for (int i = 0; i < 5; i++) {
			try {
				packets = PacketReceivedQueue.getPackets();
				for (Packet packet : packets.values()) {
					if (true == exists(packet, "/message/event/items/retract")) {
						Assert.assertTrue("Notification was sent", false);
						return;
					}
				}
			} catch (ConcurrentModificationException e) {
				// Ignore this!
			}
			Thread.sleep(40);
		}
	}

	@Test
	public void testRequestingRetractionNotificationsSendsAsRequested()
			throws Exception {

		String node = "/user/" + getUserJid(1) + "/posts";

		TestPacket makePost = getPacket("resources/channel/node/item-post/create.request");
		makePost.setVariable("$NODE", node);
		Packet postedItem = sendPacket(makePost);
		Assert.assertTrue(exists(postedItem, "/iq/pubsub/publish/item/@id"));
		String itemId = getValue(postedItem, "/iq/pubsub/publish/item/@id");

		// Subscribe user 2
		TestPacket subscribe = getPacket(
				"resources/channel/node/subscribe/success.request", 2);
		subscribe.setVariable("$NODE", node);
		sendPacket(subscribe, 2);

		TestPacket itemDelete = getPacket("resources/channel/node/item-delete/success.request");
		itemDelete.setVariable("$NODE", node);
		itemDelete.setVariable("$ITEM_ID", itemId);

		PacketReceivedQueue.clearPackets();

		Packet reply = sendPacket(itemDelete);

		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		HashMap<String, Packet> packets;
		for (int i = 0; i < 5; i++) {
			try {
				packets = PacketReceivedQueue.getPackets();
				for (Packet packet : packets.values()) {
					if ((true == packet.getTo().contains(getUserJid(2)))
							&& (true == exists(packet,
									"/message/event/items/retract"))) {
						Assert.assertTrue(exists(packet, "/message"));
						Assert.assertEquals("headline",
								getValue(packet, "/message/@type"));
						Assert.assertEquals(
								itemId,
								getValue(packet,
										"/message/event/items/retract/@id"));
						Assert.assertEquals(node,
								getValue(packet, "/message/event/items/@node"));
						return;
					}
				}
			} catch (ConcurrentModificationException e) {
				// Ignore this!
			}
			Thread.sleep(40);
		}
		Assert.assertFalse("No notification message was sent", true);
	}
}