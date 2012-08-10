package org.buddycloud.channelserver.channel.node;

import org.buddycloud.channelserver.ChannelServerTestHelper;
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

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    }
    
    @Test
    public void canPostAReplyTest() throws Exception
    {
    	Packet packet = getPacket("resources/channel/node/create-post.request");
		Packet reply  = sendPacket(packet);
    	packet.setVariable("$IN_REPLY_TO", getValue(reply, "/iq/pubsub/publish/item[@id]"));

    	Packet packet = getPacket("resources/channel/node/create-post.request");
    	Packet reply  = sendPacket(packet);
		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish/item[@id]"));
		Assert.assertTrue(exists(reply, "/iq/pubsub/publish[@node]"));
    	System.out.println(getValue(reply, "/iq/pubsub/publish/item[@id]"));
    }
}