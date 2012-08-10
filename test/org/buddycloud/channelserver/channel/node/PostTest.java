package org.buddycloud.channelserver.channel.node;

import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;

import junit.framework.Assert;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 *
 */
public class PostTest 
    extends ChannelServerTestHelper
{
    @Test
    public void canPostToPostsNodeTest() throws Exception
    {
    	Packet packet = getPacket("resources/channel/node/create-post.request");
		Packet reply  = sendPacket(packet);
		System.out.println("---------------- received");
System.out.println(reply);
		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
    
    @Test
    public void canPostAReplyTest() throws Exception
    {
    	Packet packet   = getPacket("resources/channel/node/create-post.request");
		Packet response = sendPacket(packet);
		String postId   = getValue(response, "/iq/pubsub/publish/item[@id]");

    	TestPacket followUp = getPacket("resources/channel/node/create-post.request");
    	followUp.setVariable("$IN_REPLY_TO", postId);
    	Packet reply = sendPacket(followUp);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    	System.out.println(getValue(reply, "/iq/pubsub/publish/item[@id]"));
    }
}