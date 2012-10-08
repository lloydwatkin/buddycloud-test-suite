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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Represents the event where configuration of a node is updated.
 * 
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 */
public class Configuration implements PacketExtension
{
	protected String node;
	
	/**
	 * Constructs a configuration.
	 * 
	 * @param node The node that was configured
	 */
	public Configuration(String node) {
		this.node = node;
	}
		
	public String getElementName()
	{
		return "configuration";
	}

	public String getNamespace()
	{
		return null;
	}

	public String toXML()
	{
		StringBuilder builder = new StringBuilder("<");
		builder.append(getElementName());
		appendAttribute(builder, "node", node);
		builder.append("/>");
		return builder.toString();
	}

	private void appendAttribute(StringBuilder builder, String att, String value)
	{
		builder.append(" ");
		builder.append(att);
		builder.append("='");
		builder.append(value);
		builder.append("'");
	}
}