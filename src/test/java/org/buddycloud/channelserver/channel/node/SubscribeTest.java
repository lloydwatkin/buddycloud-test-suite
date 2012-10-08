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
	public void testCanSubscribeToPrivateChannel() throws Exception {
		
		String affiliation = "publisher";
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success.request");
		makeNodePrivate.setVariable("$AFFILIATION", affiliation);
		makeNodePrivate.setVariable("$ACCESS_MODEL", "authorize");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
		
		TestPacket postItem = getPacket("resources/channel/node/item-post/create.request");
		postItem.setVariable("$NODE", node);
		Packet postItemReply = sendPacket(postItem);
		String itemId = getValue(postItemReply, "/iq/pubsub/publish/item/@id");
		
		// Subscribe to the node 
		TestPacket subscribeToNode = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribeToNode.setVariable("$NODE",  node);
		Packet reply = sendPacket(subscribeToNode, 2);
		
		Assert.assertEquals("result", getValue(reply, "/iq/@type"));
		Assert.assertEquals("pending", getValue(reply, "/iq/pubsub/subscription/@subscription"));
		Assert.assertEquals(node, getValue(reply, "/iq/pubsub/subscription/@node"));
		
		Assert.assertEquals(affiliation, getValue(reply, "/iq/pubsub/affiliation/@affiliation"));
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

	@Test
	public void testOutcastCanNotSubscribe() throws Exception {
		
		// Create a new node
		String node = createNode();
		
		// Subscribe to the node
		TestPacket subscribeToNode = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribeToNode.setVariable("$NODE",  node);
		sendPacket(subscribeToNode, 2);

		// Make user an outcast
		TestPacket makeOutcast = getPacket("resources/channel/node/affiliation/make-outcast.request");
		makeOutcast.setVariable("$NODE",  node);
		makeOutcast.setVariable("$SUBSCRIBING_JID", "\\$USER2_JID");
		variableReplacement(makeOutcast, 1, null);
        sendPacket(makeOutcast);
        
        // Unsubscribe user
		TestPacket unsubscribeUser = getPacket("resources/channel/node/subscribe/deny-request.request");
		unsubscribeUser.setVariable("$NODE",  node);
		unsubscribeUser.setVariable("$SUBSCRIBING_JID", "\\$USER2_JID");
		variableReplacement(unsubscribeUser, 1, null);
        sendPacket(unsubscribeUser);
        
        // Attempt to subscribe again
		TestPacket subscribeToNodeAgain = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribeToNodeAgain.setVariable("$NODE",  node);
		Packet reply = sendPacket(subscribeToNodeAgain, 2);
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply,
				"/iq[@type='error']/error[@type='AUTH']/forbidden"));
	}
	
	@Test
	public void testDenyingSubscriptionRequestPreventsUserFromUsing() throws Exception {
		
		String affiliation = "member";
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success.request");
		makeNodePrivate.setVariable("$AFFILIATION", affiliation);
		makeNodePrivate.setVariable("$ACCESS_MODEL", "authorize");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
		
		// Subscribe to the node 
		TestPacket subscribeToNode = getPacket("resources/channel/node/subscribe/success.request", 2);
		subscribeToNode.setVariable("$NODE",  node);
		Packet reply = sendPacket(subscribeToNode, 2);
		
	    // Deny subscription
		TestPacket denySubscription = getPacket("resources/channel/node/subscribe/deny-request.request");
		denySubscription.setVariable("$NODE",  node);
		denySubscription.setVariable("$SUBSCRIBING_JID", "\\$USER2_JID");
		variableReplacement(denySubscription, 1, null);
		sendPacket(denySubscription);
		
		// Post to the node
		TestPacket subscriberPostItem = getPacket("resources/channel/node/item-post/create.request", 2);
		subscriberPostItem.setVariable("$NODE", node);
		Packet subscriberPostItemReply = sendPacket(subscriberPostItem, 2);
		
		Assert.assertEquals("error", getValue(subscriberPostItemReply, "/iq/@type"));
		Assert.assertTrue(exists(subscriberPostItemReply,
				"/iq[@type='error']/error[@type='AUTH']/forbidden"));
	}
}