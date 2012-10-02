/**
 * $RCSfile$
 * $Revision:$
 * $Date:$
 *
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

package org.jivesoftware.smackx.pubsub.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.Item;

/**
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 */
public class Publish implements PacketExtension {

	public static final String PUBLISH = "publish";

	Item item;
    String node;
    
	/**
	 * Adds a new item to the publish element
	 * 
	 * @param Item the item
	 */
	public void addItem(Item item) {
		this.item = item;
	}
	
	/**
	 * Set node
	 * 
	 * @param String node 
	 */
	public void setNode(String node) {
		this.node = node;
	}

	public String getElementName() {
		return PUBLISH;
	}

	public String getNamespace() {

		return PubSubNamespace.BASIC.getXmlns();
	}

	public String toXML() {
		StringBuilder builder = new StringBuilder("<" + getElementName());
        if (null != node) {
        	builder.append(" node=\"" + node + "\"");
        }
		builder.append(">");
		if (null != item) {
			builder.append(item.toXML());
		}
		builder.append("</" + getElementName() + ">");
		return builder.toString();
	}
}