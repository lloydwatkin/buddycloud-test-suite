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
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import org.buddycloud.channelserver.PacketReceivedQueue;
import org.buddycloud.channelserver.TestContext;

import org.apache.commons.io.IOUtils;
import org.jaxen.JaxenException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.SyncPacketSend;

/**
 * @author Abmar
 * 
 */
public class XMPPAcceptanceTestHelper {

	private TestContext tc;
	protected XMPPConnection xmppConnection;

	private final static Logger LOGGER = Logger
			.getLogger("XMPPAcceptanceTestHelper");

	/**
	 * Initialise XMPP connection
	 * 
	 * @throws Exception
	 */
	protected void initConnection() throws Exception {
		ConnectionConfiguration cc = new ConnectionConfiguration(
				tc.getServerHostname(), tc.getServerPort());

		this.xmppConnection = new XMPPConnection(cc);
		xmppConnection.connect();
		xmppConnection.login(tc.getClientUser(), tc.getClientPass(),
				tc.getClientResource());

		xmppConnection.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				LOGGER.debug("    --- Receiving packet ---");
				LOGGER.debug(packet.toXML());
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return packet instanceof IQ;
			}
		});
		xmppConnection.addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				LOGGER.debug("    --- Sending packet ---");
				LOGGER.debug(packet.toXML());
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return packet instanceof IQ;
			}
		});
		Packet packet = getPacket("resources/register/register.request");
		sendPacket(packet);
	}

	public void setContext(TestContext tc) {
		this.tc = tc;
	}

	public TestPacket preparePacket(String packetXml) {

		packetXml = packetXml.replaceAll(".*>(.*)<[^/].*", "")
				.replaceAll("/^.*>([^>]*)$/m", "")
				.replaceAll("/^([^<]*)<.*$/m", "").replaceAll("\n", "")
				.replaceAll("\r", "");
		TestPacket p = new TestPacket(packetXml);

		String id = Packet.nextID();
		Map<String, String> map = tc.toMap();
		map.put("$ID", id);
		for (Entry<String, String> entry : map.entrySet()) {
			p.setVariable(entry.getKey(), entry.getValue());
		}

		p.setPacketID(id);
		p.setTo(tc.getTo());

		return p;
	}

	protected TestPacket getPacket(String stanzaFile) throws IOException {
		String requestXml = IOUtils.toString(new FileInputStream(stanzaFile));
		return preparePacket(requestXml);
	}

	protected Packet sendPacket(Packet p) throws Exception {
		Packet reply = null;
		try {
			reply = SyncPacketSend.getReply(xmppConnection, p);
			if (reply.getPacketID().toString()
					.equals(p.getPacketID().toString())) {
				return reply;
			}
			return PacketReceivedQueue.getPacketWithId(p.getPacketID());
		} catch (Exception e) {
			throw new Exception(e.getMessage() + "\nPacket sent:\n" + p.toXML());
		}
	}

	protected String getValue(Packet p, String xPath) throws JDOMException,
			IOException, JaxenException {
		Object evaluateFirst = getEl(p, xPath);
		if (evaluateFirst instanceof Attribute) {
			Attribute attribute = (Attribute) evaluateFirst;
			return attribute.getValue();
		} else if (evaluateFirst instanceof Element) {
			throw new RuntimeErrorException(null, "XPath maps to element, not attribute");
		}
		System.out.println(evaluateFirst.getClass().getName());
		return evaluateFirst == null ? null : ((Attribute) evaluateFirst).getValue();
	}

	protected String getText(Packet p, String xPath) throws JDOMException,
			IOException, JaxenException {
		Text evaluateFirst = (Text) getEl(p, xPath);
		return evaluateFirst == null ? null : evaluateFirst.getValue();
	}

	protected boolean exists(Packet p, String xPath) throws JDOMException,
			IOException, JaxenException {
		return getEl(p, xPath) != null;
	}

	private Object getEl(Packet p, String xPath) throws JDOMException,
			IOException {
		SAXBuilder saxBuilder = new SAXBuilder();
		saxBuilder.setFeature("http://xml.org/sax/features/namespaces", false);
		Document replyDoc = saxBuilder.build(IOUtils.toInputStream(p.toXML()));
		Object evaluateFirst = XPathFactory.instance().compile(xPath)
				.evaluateFirst(replyDoc);
		return evaluateFirst;
	}
}
