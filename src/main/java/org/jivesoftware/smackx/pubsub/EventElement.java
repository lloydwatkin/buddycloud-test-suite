/**
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
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
package org.jivesoftware.smackx.pubsub;

import java.util.Arrays;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

/**
 * Represents the top level element of a pubsub event extension.  All types of pubsub events are
 * represented by this class.  The specific type can be found by {@link #getEventType()}.  The 
 * embedded event information, which is specific to the event type, can be retrieved by the {@link #getEvent()}
 * method.
 * 
 * @author Robin Collier
 */
public class EventElement implements EmbeddedPacketExtension
{
	private EventElementType type;
	private List<? extends PacketExtension> extensions;
	
	public EventElement(EventElementType eventType, PacketExtension eventExt)
	{
		type = eventType;
		extensions = Arrays.asList(eventExt);
	}
	
	public EventElement(EventElementType eventType, List<? extends PacketExtension> packetExtensions) {
		type = eventType;
		extensions = packetExtensions;
	}
	
	public EventElementType getEventType()
	{
		return type;
	}

	public List<PacketExtension> getExtensions()
	{
		return (List<PacketExtension>) extensions;
	}

	public String getElementName()
	{
		return "event";
	}

	public String getNamespace()
	{
		return PubSubNamespace.EVENT.getXmlns();
	}

	public String toXML()
	{
		StringBuilder builder = new StringBuilder("<event xmlns='" + PubSubNamespace.EVENT.getXmlns() + "'>");
        for (PacketExtension extension : extensions) {
		    builder.append(extension.toXML());
        }
		builder.append("</event>");
		return builder.toString();
	}
}
