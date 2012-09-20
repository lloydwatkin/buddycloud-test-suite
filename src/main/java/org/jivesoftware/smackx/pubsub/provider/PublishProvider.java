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
package org.jivesoftware.smackx.pubsub.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.pubsub.packet.Publish;
import org.xmlpull.v1.XmlPullParser;

/**
 * Parses a <b>publish</b> element as is defined in both the
 * {@link PubSubNamespace#BASIC} and {@link PubSubNamespace#EVENT} namespaces.
 * To parse the publish element content, it will use whatever
 * {@link PacketExtensionProvider} is registered in <b>smack.providers</b> for
 * its element name and namespace. If no provider is registered, it will return
 * a {@link SimplePayload}.
 * 
 * The smack.properties entry would be as follows:
 * 
 * &lt;extensionProvider&gt; &lt;elementName&gt;publish&lt;/elementName&gt;
 * &lt;namespace&gt;http://jabber.org/protocol/pubsub&lt;/namespace&gt;
 * &lt;className
 * &gt;org.jivesoftware.smackx.pubsub.provider.PublishProvider&lt;/className&gt;
 * &lt;/extensionProvider&gt;
 * 
 * @author Lloyd Watkin <lloyd.watkin@surevine.com>
 */
public class PublishProvider implements PacketExtensionProvider {
	
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		
		Publish publish = new Publish();
		publish.setNode(parser.getAttributeValue(null, "node"));
		
		int tag = parser.next();
		if (tag != XmlPullParser.END_TAG) {

			Item item = new Item();
			publish.addItem(item);

            if (true == parser.getName().equals("item")) {
            	String id = parser.getAttributeValue(null, "id");
            	item = new Item(id);
            	publish.addItem(item);
            }
		}
	    return publish;
	}
}