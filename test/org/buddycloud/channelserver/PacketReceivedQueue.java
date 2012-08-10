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

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 */
public class PacketReceivedQueue
{
	private static HashMap<String, TestPacket> packets;

	/**
	 * Add a received packet

	 */
	public static void addPacket(TestPacket packet)
	{
	   packets.put(packet.getPacketID(), packet);
	}

	public static HashMap<String, TestPacket> getPackets()
	{		
		return packets;
	}
	
	public static TestPacket getPacketWithId(String id)
	    throws InvalidParameterException, InterruptedException
	{
		return getPacketWithId(id, 5000);
	}

	public static TestPacket getPacketWithId(String id, long timeout) 
        throws InvalidParameterException, InterruptedException 
	{
		long t   = System.currentTimeMillis();
		long end = t + timeout;
		while (System.currentTimeMillis() < end) {
	        for (Map.Entry<String, TestPacket> packet : packets.entrySet()) {
		        if (packet.getKey() == id) {
		            return packet.getValue();
		        }
			}
		  Thread.sleep(20);
		}
		throw new InvalidParameterException("Packet not received...");
	}

	public static void clearPackets()
	{
		for (Map.Entry<String, TestPacket> packet : packets.entrySet()) {
	        packets.remove(packet.getKey());
		}
	}

	public static void removePacket(String id)
	{
		for (Map.Entry<String, TestPacket> packet : packets.entrySet()) {
			if (packet.getKey() == id) {
	            packets.remove(packet.getKey());
			}
		}
	}
}
