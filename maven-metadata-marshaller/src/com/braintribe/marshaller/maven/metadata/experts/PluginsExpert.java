// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
