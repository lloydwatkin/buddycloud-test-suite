package org.buddycloud.channelserver.channel.node;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.buddycloud.channelserver.ChannelServerTestHelper;
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
		
		Packet packet = getPacket("resources/channel/node/subscribe/missing-nodeid.request");
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
	@Ignore("Not ready yet")
	public void testTryingToSubscribeToNonExistentNodeReturnsErrorStanza()
			throws Exception {
		
		// System.out.println("\n\n" + packet.toXML() + "\n");
		// System.out.println(reply.toXML());
	}

	@Test
	@Ignore("Require other functionality first")
	public void testOutcastCanNotSubscribe() throws Exception {
	}

	@Test
	@Ignore("Not ready yet")
	public void testAttemptingToSubscribeWhenSubscribedReturnsErrorStanza()
			throws Exception {
	}

	@Test
	@Ignore("Not ready yet")
	public void testSuccesfulSubscriptionReturnsExceptedResponse()
			throws Exception {
	}

	@Test
	@Ignore("Not ready yet - can we do this without having multiple connections?")
	public void testNotificationsAreSentUponSubscription() throws Exception {
	}
}