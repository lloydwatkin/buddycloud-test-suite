package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 *
 */
public class PostTest 
    extends ChannelServerTestHelper
{
	private static final Logger LOGGER = Logger.getLogger(PostTest.class);
	
    @Test
    public void canPostToPostsNodeTest() throws Exception
    {
    	Packet packet = getPacket("resources/channel/node/create-post.request");
		Packet reply  = sendPacket(packet);
      
		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
    
    @Test
    public void canPostAReplyTest() throws Exception
    {
    	Packet packet   = getPacket("resources/channel/node/create-post.request");
		Packet response = sendPacket(packet);
		String postId   = getValue(response, "/iq/pubsub/publish/item/@id");

    	TestPacket followUp = getPacket("resources/channel/node/create-reply.request");
        followUp.setVariable("$IN_REPLY_TO", postId);

    	Packet reply = sendPacket(followUp);

		Assert.assertEquals(reply.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
}