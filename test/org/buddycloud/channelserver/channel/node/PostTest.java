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
	/** 
	 * @response:
	 *     <iq to='twinkle@ip-10-66-2-93/tigase-1' id='3402:pubsubpublishnode' from='channels.ip-10-66-2-93' type='result'>
	 *         <pubsub xmlns='http://jabber.org/protocol/pubsub'>
	 *             <publish node='/user/twinkle@ip-10-66-2-93/posts'>
	 *                 <item id='f6be9770-e7b4-4287-b726-3c9df6bab1bc'/>
	 *             </publish>
	 *         </pubsub>
	 *     </iq>
	 */
    @Test
    public void canPostToPostsNodeTest() throws Exception
    {
    	Packet packet = getPacket("resources/channel/node/create-post.request");
		Packet reply  = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		
		Assert.assertTrue(exists(reply, "/iq/query/identity[@type='channels' and @category='pubsub']"));
    }
    
    
}