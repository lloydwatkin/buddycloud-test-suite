/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.buddycloud.channelserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathFactory;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.SyncPacketSend;

/**
 * @author Abmar
 * 
 */
public class XMPPAcceptanceTestHelper {

	private TestContext user1;
	private TestContext user2;
	
	protected static XMPPConnection[] xmppConnection = new XMPPConnection[2];

	private final static Logger LOGGER = Logger
			.getLogger("XMPPAcceptanceTestHelper");

	/**
	 * Initialise XMPP connection
	 * 
	 * @throws Exception
	 */
	protected void initConnection() throws Exception {
		
		createConnection(1);
		createConnection(2);
	}

	private void createConnection(final int i) throws Exception {
		int arrayOffset = i - 1;
		
		// Don't connect and register for every test!
		if (null != this.xmppConnection[arrayOffset]) {
			return;
		}
		ConnectionConfiguration cc = new ConnectionConfiguration(
				user1.getServerHostname(), user1.getServerPort());

		this.xmppConnection[arrayOffset] = new XMPPConnection(cc);
		
		TestContext user = user1;
		if (2 == i) {
			user = user2;
		}
		xmppConnection[arrayOffset].connect();
		xmppConnection[arrayOffset].login(user.getClientUser(), user.getClientPass(),
				user.getClientResource());

		final String userJid = user.getClientUser();
		xmppConnection[arrayOffset].addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet.getTo().contains(userJid)) {
					PacketReceivedQueue.addPacket(packet);
					LOGGER.debug("    --- Receiving packet for user" + i + " ---");
					LOGGER.debug(packet.toXML());
				}
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return (packet.getTo().contains(userJid));
				//return ((packet instanceof IQ) && (packet.getTo().contains(userJid)));
			}
		});
		xmppConnection[arrayOffset].addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet.getFrom().contains(userJid)) {
					LOGGER.debug("    --- Sending packet for user" + i + " ---");
					LOGGER.debug(packet.toXML());
				}
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return ((packet instanceof IQ) && (null != packet.getTo()) && (true == packet.getTo().contains(userJid)));
			}
		});
		Packet packet = getPacket("resources/register/register.request");
		sendPacket(packet, i, (long) 10000);
	}

	public void setUsers(TestContext user1, TestContext user2) {
		this.user1 = user1;
		this.user2 = user2;
	}

	public TestPacket preparePacket(String packetXml) {
		return preparePacket(packetXml, 1);
	}
	
	public TestPacket preparePacket(TestPacket packet) {
	     	return preparePacket(packet.toXML());
	}
	
	public TestPacket preparePacket(String packetXml, int userNumber) {
		TestContext user = user1;
		if (2 == userNumber) {
			user = user2;
		}
		packetXml = packetXml
				.replaceAll(">\\s+<", "><")
				.replaceAll("\n", "")
				.replaceAll("\r", "");

		TestPacket p = new TestPacket(packetXml);
		
		String id = Packet.nextID();
		p.setPacketID(id);
		p.setTo(user.getTo());

		variableReplacement(p, userNumber, id);

		return p;
	}

	public void variableReplacement(TestPacket p, int userNumber, String packetId) {
		
		TestContext user = user1;
		if (2 == userNumber) {
			user = user2;
		}
		Map<String, String> map = user.toMap();
		if (null != packetId) {
			map.put("$ID", packetId);
		}
		map.put("$USER1_JID", user1.getClientUser() + "@" + user1.getServiceName());
		map.put("$USER2_JID", user2.getClientUser() + "@" + user2.getServiceName());
		for (Entry<String, String> entry : map.entrySet()) {
			p.setVariable(entry.getKey(), entry.getValue());
		}
	}
	
	public String getUserJid(int userNumber) {
	    TestContext user = user1;
	    if (2 == userNumber) {
	    	user = user2;
	    }
	    return user.getClientUser() + "@" + user.getServiceName();
	}

	protected TestPacket getPacket(String stanzaFile, int userNumber) throws IOException {
		return preparePacket(getPacketXml(stanzaFile), userNumber);
	}
	protected TestPacket getPacket(String stanzaFile) throws IOException {
		return getPacket(stanzaFile, 1);
	}
	
	public String getPacketXml(String stanzaFile) throws FileNotFoundException, IOException {
		return IOUtils.toString(new FileInputStream(stanzaFile));
	}

	protected Packet sendPacket(Packet p) throws Exception {
		return sendPacket(p, 1);
	}
	
	protected Packet sendPacket(Packet p, int userNumber, long timeout) throws Exception {
		
		Connection connection;
		if (1 == userNumber) {
			connection = this.xmppConnection[0];
		} else {
			connection = this.xmppConnection[1];
		}
		
		Packet reply = null;
		try {
			reply = SyncPacketSend.getReply(connection, p, timeout, false);
			if (reply.getPacketID().toString()
					.equals(p.getPacketID().toString())) {
				return reply;
			}
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return PacketReceivedQueue.getPacketWithId(p.getPacketID());
	}
	
	protected Packet sendPacket(Packet p, int userNumber) throws Exception {
		return sendPacket(p, userNumber, SmackConfiguration.getPacketReplyTimeout());
	}

	protected Packet sendPacketWithNextId(Packet p) throws Exception {

		p.setPacketID(Packet.nextID());
		return sendPacket(p);
	}
		
    protected String getValue(Packet p, String xPath) throws Exception {
    	return getValue(p, xPath, false);
    }
    	
    protected String getValue(Packet p, String xPath, boolean namespaceFeature) throws Exception {
		Object evaluateFirst = getEl(p, xPath, namespaceFeature);
		if (evaluateFirst instanceof Attribute) {
			Attribute attribute = (Attribute) evaluateFirst;
			return attribute.getValue();
		} else if (evaluateFirst instanceof Text) {
			return ((Text) evaluateFirst).getText();
		} else if (evaluateFirst instanceof Element) {
			throw new RuntimeErrorException(null, "XPath maps to element, not attribute");
		}
		return evaluateFirst == null ? null : ((Attribute) evaluateFirst).getValue();
	}

	protected String getText(Packet p, String xPath) throws Exception {
		Text evaluateFirst = (Text) getEl(p, xPath);
		return evaluateFirst == null ? null : evaluateFirst.getValue();
	}

	protected boolean exists(Packet p, String xPath) throws Exception {
		return exists(p, xPath, false);
	}
	
	protected boolean exists(Packet p, String xPath, boolean namespaceFeature) throws Exception {
		return getEl(p, xPath, namespaceFeature) != null;
	}

	private Object getEl(Packet p, String xPath) throws Exception {
		return getEl(p, xPath, false);
	}
	
	private Object getEl(Packet p, String xPath, boolean namespaceFeature) throws Exception {
		
		InputStream xmlStream = IOUtils.toInputStream(p.toXML());
		SAXBuilder saxBuilder = new SAXBuilder();
	    saxBuilder.setFeature("http://xml.org/sax/features/namespaces", namespaceFeature);
	    saxBuilder.setFeature("http://xml.org/sax/features/validation",  false);
	    saxBuilder.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
	    Document replyDoc = (Document) saxBuilder.build(xmlStream);
		return XPathFactory.instance().compile(xPath)
				.evaluateFirst(replyDoc);
	}
}