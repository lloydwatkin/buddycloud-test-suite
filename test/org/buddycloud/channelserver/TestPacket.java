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

import org.jivesoftware.smack.packet.Packet;

/**
 * @author Abmar
 *
 */
public class TestPacket extends Packet {

	private String xml;
	
	public TestPacket(String xml) {
		this.xml = xml;
	}

	public void setVariable(String variable, String value) {
		this.xml = xml.replaceAll("\\" + variable, value);
	}
	
	/* (non-Javadoc)
	 * @see org.jivesoftware.smack.packet.Packet#toXML()
	 */
	@Override
	public String toXML() {
		return xml;
	}

}
