package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 *
 */
public class PostTest 
    extends ChannelServerTestHelper
{
	private static final Logger LOGGER = Logger.getLogger(PostTest.class);
	
    @Test
    public void testCanPostToNode() throws Exception
    {
    	String node   = createNode();
    	TestPacket packet = getPacket("resources/channel/node/item-post/create.request");
    	packet.setVariable("$NODE", node);
		Packet reply  = sendPacket(packet);
      
		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
    
    @Test
    public void testCanPostAReply() throws Exception
    {
    	String node   = createNode();
    	TestPacket packet = getPacket("resources/channel/node/item-post/create.request");
    	packet.setVariable("$NODE", node);
		Packet response = sendPacket(packet);
		String postId   = getValue(response, "/iq/pubsub/publish/item/@id");

    	TestPacket followUp = getPacket("resources/channel/node/item-post/reply.request");
    	followUp.setVariable("$NODE", node);
        followUp.setVariable("$IN_REPLY_TO", postId);

    	Packet reply = sendPacket(followUp);

		Assert.assertEquals(reply.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
    
    @Test
    public void testNotProvidingNodeReturnsErrorStanza() throws Exception {

    	TestPacket packet = getPacket("resources/channel/node/item-post/missing-node-id.request");
		Packet reply = sendPacket(packet);
		      
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/bad-request"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='MODIFY']/nodeid-required"));
    }
    
    @Test
    public void testPostingToUnsubscribedNodeReturnsError() throws Exception {
    	String node   = createNode();
    	
    	TestPacket packet = getPacket("resources/channel/node/item-post/create.request");
    	packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet, 2);
		
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/forbidden"));
    }
    
    @Test
    public void testPostingToNodeWithMemberAffiliationReturnsError() throws Exception {
		String node = createNode();
		TestPacket makeNodePrivate = getPacket("resources/channel/node/configure/success.request");
		makeNodePrivate.setVariable("$AFFILIATION", "member");
		makeNodePrivate.setVariable("$ACCESS_MODEL", "open");
		makeNodePrivate.setVariable("$NODE", node);
		sendPacket(makeNodePrivate);
    	
    	TestPacket packet = getPacket("resources/channel/node/item-post/create.request");
    	packet.setVariable("$NODE", node);
		Packet reply = sendPacket(packet, 2);
		
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='AUTH']/forbidden"));
    }
    
    @Test
    public void testPostingToNodeWhichDoesntExistReturnsErrorStanza() throws Exception {
    	TestPacket packet = getPacket("resources/channel/node/item-post/not-existing-node.request");
		Packet reply = sendPacket(packet);
		      
		Assert.assertEquals("error", getValue(reply, "/iq/@type"));
		Assert.assertTrue(exists(reply, "/iq/error[@type='CANCEL']/item-not-found"));
    }
}