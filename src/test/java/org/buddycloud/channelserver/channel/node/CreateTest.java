package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.buddycloud.channelserver.ChannelServerTestHelper;
import org.buddycloud.channelserver.TestPacket;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;

public class CreateTest extends ChannelServerTestHelper
{
	@Test
    public void testCanCreateNode() throws Exception
    {
    	TestPacket packet = getPacket("resources/channel/node-create.request");
    	packet.setVariable("$NODE", "post");
		Packet reply  = sendPacket(packet);

		Assert.assertEquals(packet.getPacketID(), getValue(reply, "/iq/@id"));
		Assert.assertEquals(packet.getProperty("type"), "result");
    }
}
