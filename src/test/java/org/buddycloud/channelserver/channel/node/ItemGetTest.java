package org.buddycloud.channelserver.channel.node;

import java.io.IOException;

import junit.framework.Assert;

import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.buddycloud.channelserver.XMPPAcceptanceTestHelper;
import org.jaxen.JaxenException;
import org.jdom2.JDOMException;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Ignore;
import org.junit.Test;

public class ItemGetTest extends ChannelServerTestHelper {
	
	@Test
	public void testNotPassingNodeReturnsExpectedError() throws Exception {
		
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/missing-node-id.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		/**
		 * XPath should be: /iq/error[@type='MODIFY']/bad-request[@xmlns='urn:ietf:params:xml:ns:xmpp-stanzas']/
		 * But the XMLNS attribute causes issues so skipping for now
		 */
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad_request"));
		// /iq/error[@type='MODIFY']/nodeid-required[@xmlns='http://jabber.org/protocol/pubsub#errors']
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/nodeid-required"));
	}
	
	@Test
	public void testRequestingItemsForNotExistingNodeReturnsExpectedError() throws Exception {
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/not-existing-node.request");
		Packet reply = sendPacket(packet);

		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		/**
		 * XPath should be: /iq/error[@type='CANCEL']/item-not-found[@xmlns='urn:ietf:params:xml:ns:xmpp-stanzas']/
		 * But the XMLNS attribute causes issues so skipping for now
		 */
		Assert.assertTrue(exists(reply, "/iq/error[@type='CANCEL']/item-not-found"));
	}
	
	@Test
	public void testAttemptToGetItemsFromOpenChannelWithNoItemsWorks() throws Exception {
		
		String node = createNode();
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/success.request");
		packet.setVariable("$NODE", node);
		
		Packet reply = sendPacket(packet, 2);
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/items[@node]"));
	}
	
	@Test
	public void testAttemptToGetItemsFromOpenChannelWithItemWorks() throws Exception {
		
		String node = createNode();
		TestPacket postItem = getPacket("resources/channel/node/item-post/create.request");
		postItem.setVariable("$NODE", node);
		Packet postItemReply = sendPacket(postItem);
		String itemId = getValue(postItemReply, "/iq/pubsub/publish/item/@id");
		
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/success.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet, 2);
		
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertEquals(itemId, getValue(reply, "/iq/pubsub/items/item/@id"));
		
		Assert.assertTrue(exists(reply, "/iq/pubsub/items/item/entry/author/name"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/items/item/entry/content"));
		Assert.assertEquals("Post", getValue(reply, "/iq/pubsub/items/item/entry/title/text()"));
		Assert.assertEquals("post", getValue(reply, "/iq/pubsub/items/item/entry/verb/text()"));
	}
	
	@Test
	public void testAttemptToGetItemsFromPrivateUnsubscribedChannelFails() throws Exception {
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success-private.request");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
		
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/success.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet, 2);
		
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/forbidden"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/closed-node"));
	}
	
	@Test
	public void testCanGetItemsFromSubscribedPrivateChannel() throws Exception {
		
		// Create a private node and post to it
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success-private.request");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
		TestPacket postItem = getPacket("resources/channel/node/item-post/create.request");
		postItem.setVariable("$NODE", node);
		Packet postItemReply = sendPacket(postItem);
		String itemId = getValue(postItemReply, "/iq/pubsub/publish/item/@id");
		
		// Subscribe to the node and retrieve posts
		TestPacket subscribeToNode = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribeToNode.setVariable("$NODE",  node);
		sendPacket(subscribeToNode, 2);
		
		// Approve subscription
		TestPacket approveSubscription = getPacket("resources/channel/node/subscribe/approve-request.request");
		approveSubscription.setVariable("$NODE",  node);
		approveSubscription.setVariable("$SUBSCRIBING_JID", "\\$USER2_JID");
		variableReplacement(approveSubscription, 1, null);
        sendPacket(approveSubscription);
		
		TestPacket packet = getPacket("resources/channel/node/item-retrieval/success.request");
		packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet, 2);
		
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertEquals(itemId, getValue(reply, "/iq/pubsub/items/item/@id"));
	}
}