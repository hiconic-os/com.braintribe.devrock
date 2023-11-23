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
package com.braintribe.marshaller.artifact.maven.settings;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.settings.experts.HasSettingsTokens;
import com.braintribe.marshaller.artifact.maven.settings.experts.SettingsExpert;
import com.braintribe.model.artifact.maven.settings.Settings;

/**
 * an {@link XMLStreamReader} based POM marshaller (actually, currently only a reader) which returns a RAW solution,
 * i.e. now smart resolving takes place (parent dependency, not resolved parent solution; raw properties, no resolved
 * properties etc)
 * 
 * @author pit
 *
 */
public class DeclaredMavenSettingsMarshaller implements Marshaller, HasSettingsTokens {
	private static Logger log = Logger.getLogger(DeclaredMavenSettingsMarshaller.class);
	private static XMLInputFactory inputFactory;

	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = log.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch (Exception e) {
			if (debug)
				log.debug("Could not set feature " + XMLInputFactory.SUPPORT_DTD + "=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch (Exception e) {
			if (debug)
				log.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}

	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		throw new UnsupportedOperationException("not implemented");

	}
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Settings unmarshall(InputStream in) throws MarshallException {
		return (Settings) unmarshall( in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
			BasicSettingsMarshallerContext context = new BasicSettingsMarshallerContext(new StringBuilder(), options.getSession());
			Settings settings = null;
			while (reader.hasNext()) {
				switch (reader.getEventType()) {
					case XMLStreamConstants.START_ELEMENT:
						String tag = reader.getName().getLocalPart(); {
						if (tag.equalsIgnoreCase(SETTINGS)) {
							settings = SettingsExpert.read(context, reader);
						}
						break;
					}
					case XMLStreamConstants.END_ELEMENT: {
						return settings;
					}
					case XMLStreamConstants.END_DOCUMENT: {
						return settings;
					}
					default:
						break;
				}
				reader.next();
			}
			return settings;
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		}
	}

}
