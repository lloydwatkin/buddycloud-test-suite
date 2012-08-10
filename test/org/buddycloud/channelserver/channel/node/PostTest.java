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

		Assert.assertTrue(exists(reply, "/message/event/items[0]/item[@id]"));
		Assert.assertEquals("post", getText(reply, "/message/event/items[0]/item/verb/text()"));
		
		/**
		 * <message from='channels.ip-10-66-2-93' to='twinkle@ip-10-66-2-93/tigase-3' type='headline'>
		 *     <event xmlns='http://jabber.org/protocol/pubsub#event'>
		 *         <items node='/user/twinkle@ip-10-66-2-93/posts'>
		 *             <item id='504794b9-c07a-4ddd-8eb6-eaeb74f6e8e6'>
		 *                 <entry xmlns='http://www.w3.org/2005/Atom' xmlns:thr='http://purl.org/syndication/thread/1.0'>
		 *                     <content>post</content>
		 *                     <author>
		 *                         <name>twinkle@ip-10-66-2-93</name>
		 *                         <uri>acct:twinkle@ip-10-66-2-93</uri>
		 *                     </author>
		 *                     <id>504794b9-c07a-4ddd-8eb6-eaeb74f6e8e6</id>
		 *                     <published>2012-08-10T09:24:28.817Z</published>
		 *                     <updated>2012-08-10T09:24:28.817Z</updated>
		 *                     <link rel='self' href='xmpp:channels.ip-10-66-2-93?pubsub;action=retrieve;node=/user/twinkle@ip-10-66-2-93/posts;item=504794b9-c07a-4ddd-8eb6-eaeb74f6e8e6'/>
		 *                     <verb xmlns='http://activitystrea.ms/spec/1.0/'>post</verb>
		 *                     <object xmlns='http://activitystrea.ms/spec/1.0/'>
		 *                         <object-type>note</object-type>
		 *                     </object>
		 *                 </entry>
		 *             </item>
		 *         </items>
		 *     </event>
		 * </message>
		 */
    }
    
    @Test
    public void canPostAReplyTest() throws Exception
    {
    	Packet packet   = getPacket("resources/channel/node/create-post.request");
		Packet response = sendPacket(packet);
		Assert.assertTrue(exists(response, "/message/event/items[0]/item[@id]"));
		String postId   = getText(response, "/message/event/items[0]/item/id/text()");

    	TestPacket followUp = getPacket("resources/channel/node/create-post.request");
    	followUp.setVariable("$IN_REPLY_TO", postId);
    	Packet reply = sendPacket(followUp);

		Assert.assertTrue(exists(reply, "/message/event/items[0]/item[@id]"));
		Assert.assertEquals("post", getText(reply, "/message/event/items[0]/item/verb/text()"));
    }
}