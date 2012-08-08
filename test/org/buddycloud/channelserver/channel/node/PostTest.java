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
    }
    
    //                     <!--<thr:in-reply-to ref='ae6d3c70-0d90-4d90-a120-54fd507ac9a9'/>-->
}