package org.buddycloud.channelserver.channel.node;

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
		System.out.println("Channel create");
		System.out.println(reply);
    }
}
