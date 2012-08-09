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

import org.apache.commons.io.IOUtils;
import org.jaxen.JaxenException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathFactory;
import org.jivesoftware.smack.Connection;
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
	private XMPPConnection xmppConnection;

	/**
	 * @throws XMPPException 
	 * 
	 */
	protected void initConnection() throws XMPPException {
		ConnectionConfiguration cc = new ConnectionConfiguration(
				tc.getServerHostname(), tc.getServerPort());

		this.xmppConnection      = new XMPPConnection(cc);
		xmppConnection.connect();
		xmppConnection.login(tc.getClientUser(), tc.getClientPass(), tc.getClientResource());
		
		xmppConnection.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				System.out.println("    --- Receiving packet ---");
				System.out.println(packet.toXML());
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
				System.out.println("    --- Sending packet ---");
				System.out.println(packet.toXML());
			}
		}, new PacketFilter() {
			@Override
			public boolean accept(Packet packet) {
				return packet instanceof IQ;
			}
		});
	}

	public void setContext(TestContext tc) {
		this.tc = tc;
	}
	
	private TestPacket preparePacket(String packetXml) {
		
		packetXml = packetXml.replaceAll(".*>(.*)<[^/].*", "");
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
		String requestXml = IOUtils.toString(
				new FileInputStream(stanzaFile));
		return preparePacket(requestXml);
	}
	
	protected Packet sendPacket(Packet p) throws XMPPException {
		return SyncPacketSend.getReply(xmppConnection, p);
	}
	
	protected String getValue(Packet p, String xPath) throws JDOMException,
			IOException, JaxenException {
		Attribute evaluateFirst = (Attribute) getEl(p, xPath);
		return evaluateFirst == null ? null : evaluateFirst.getValue();
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
		Document replyDoc = saxBuilder.build(
				IOUtils.toInputStream(p.toXML()));
		Object evaluateFirst = XPathFactory.instance().compile(
				xPath).evaluateFirst(replyDoc);
		return evaluateFirst;
	}
}
