// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.marshaller.maven.metadata.experts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.marshaller.maven.metadata.HasTokens;
import com.braintribe.model.artifact.meta.Plugin;

public class PluginsExpert extends AbstractMavenMetaDataExpert implements HasTokens {

	public static List<Plugin> extract( XMLStreamReader reader) throws XMLStreamException {
		List<Plugin> plugins = new ArrayList<>();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
					case tag_plugin:
						plugins.add( PluginExpert.extract(reader));
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return plugins;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}
	public static void write(XMLStreamWriter writer, List<Plugin> value) throws XMLStreamException {
		if (value != null && value.size() > 0) {
			writer.writeStartElement(tag_plugins);
			for (Plugin plugin : value) {
				PluginExpert.write(writer, plugin);
			}
			writer.writeEndElement();
		}
	}
}
