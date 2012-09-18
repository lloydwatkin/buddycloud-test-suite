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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.Packet;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 */
public class PacketReceivedQueue {
	private static HashMap<String, Packet> packets = null;

	public static void addPacket(Packet packet) {
		if (null == packets) {
			packets = new HashMap<String, Packet>();
		}
		packets.put(packet.getPacketID(), packet);
	}

	public static HashMap<String, Packet> getPackets() {
		return packets;
	}

	public static Packet getPacketWithId(String id)
			throws InvalidParameterException, InterruptedException {
		return getPacketWithId(id, 15000);
	}

	public static Packet getPacketWithId(String id, long timeout)
			throws InvalidParameterException, InterruptedException {
		long t = System.currentTimeMillis();
		long end = t + timeout;
		while (System.currentTimeMillis() < end) {
			for (Map.Entry<String, Packet> packet : packets.entrySet()) {
				if (true == packet.getKey().toString().equals(id)) {
					return packet.getValue();
				}
			}
			Thread.sleep(5);
		}
		throw new InvalidParameterException("Packet not received in " + timeout
				+ " milliseconds...");
	}

	public static void clearPackets() {
		for (Map.Entry<String, Packet> packet : packets.entrySet()) {
			packets.remove(packet.getKey());
		}
	}

	public static void removePacket(String id) {
		for (Map.Entry<String, Packet> packet : packets.entrySet()) {
			if (packet.getKey() == id) {
				packets.remove(packet.getKey());
			}
		}
	}
}
