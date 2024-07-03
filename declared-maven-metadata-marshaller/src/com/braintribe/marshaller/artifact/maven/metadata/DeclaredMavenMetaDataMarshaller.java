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
package com.braintribe.marshaller.artifact.maven.metadata;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.commons.HasTokens;
import com.braintribe.marshaller.artifact.maven.metadata.experts.MavenMetaDataExpert;
import com.braintribe.marshaller.artifact.maven.metadata.writer.IndentingXmlStreamWriter;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;

/**
 * a marshaller for poms..
 * 
 * @author pit
 *
 */
public class DeclaredMavenMetaDataMarshaller implements Marshaller, HasTokens {
	private static Logger log = Logger.getLogger(DeclaredMavenMetaDataMarshaller.class);

	private static XMLInputFactory inputFactory;
	private static XMLOutputFactory outputFactory;
	private boolean indenting = true;

	public static final DeclaredMavenMetaDataMarshaller INSTANCE = new DeclaredMavenMetaDataMarshaller();

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
		outputFactory = XMLOutputFactory.newInstance();
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		XMLStreamWriter writer;
		try (BufferedOutputStream bout = new BufferedOutputStream(out)) {
			writer = !indenting ? outputFactory.createXMLStreamWriter(bout, "UTF-8") : new IndentingXmlStreamWriter(bout, "UTF-8");
			marshall(writer, (MavenMetaData) value);
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}
	
	@Override
	public Maybe<Object> unmarshallReasoned(InputStream in, GmDeserializationOptions options) {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
			return Maybe.complete(unmarshall(reader, options));
		} catch (XMLStreamException e) {
			ParseError reason = Reasons.build(ParseError.T).text(e.getMessage()).toReason();
			Throwable cause = e.getCause();
			if (cause != null) {
				reason.getReasons().add(InternalError.from(cause));
			}
			return reason.asMaybe();
		}
	}

	/**
	 * unmarshall the pom from a reader
	 * 
	 * @param reader
	 *            - the {@link XMLStreamReader
	 * @return - the {@link Solution}
	 * @throws XMLStreamException
	 */
	private MavenMetaData unmarshall(XMLStreamReader reader, GmDeserializationOptions options) throws XMLStreamException {

		StringBuilder commonBuilder = new StringBuilder();

		MavenMetadataReadContext context = new BasicMavenMetadataReaderContext(commonBuilder, options.getSession());
		MavenMetaData metadata = null;
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					String tag = reader.getName().getLocalPart(); {
					if (tag.equalsIgnoreCase(tag_metaData)) {
						metadata = MavenMetaDataExpert.read(context, reader);
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT: {
					return metadata;
				}
				case XMLStreamConstants.END_DOCUMENT: {
					return metadata;
				}
				default:
					break;
			}
			reader.next();
		}

		return metadata;
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
			return unmarshall(reader, options);
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		}
	}

	private void marshall(XMLStreamWriter writer, MavenMetaData value) throws XMLStreamException {
		writer.writeStartDocument();
		MavenMetaDataExpert.write(writer, value);
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

}
