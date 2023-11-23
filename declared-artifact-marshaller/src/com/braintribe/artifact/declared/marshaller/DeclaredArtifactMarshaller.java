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
package com.braintribe.artifact.declared.marshaller;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.experts.HasPomTokens;
import com.braintribe.artifact.declared.marshaller.experts.ProjectExpert;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonBuilder;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.declared.DeclaredArtifact;

/**
 * a marshaller for poms.. 
 * @author pit
 *
 */
public class DeclaredArtifactMarshaller implements Marshaller, HasPomTokens {
	private static Logger log = Logger.getLogger(DeclaredArtifactMarshaller.class);

	private static XMLInputFactory inputFactory;
	
	public static DeclaredArtifactMarshaller INSTANCE = new DeclaredArtifactMarshaller();
	
	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = log.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature "+XMLInputFactory.SUPPORT_DTD+"=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch(Exception e) {
			if (debug) log.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		throw new UnsupportedOperationException("marshalling isn't supported at this stage");		
	}
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("marshalling isn't supported at this stage");		
	}

	
	@Override
	public DeclaredArtifact unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.defaultOptions);
	}

	/**
	 * unmarshall the pom from a reader
	 * @param reader - the {@link XMLStreamReader
	 * @return - the {@link Solution}
	 * @throws XMLStreamException
	 */
	private DeclaredArtifact unmarshall( XMLStreamReader reader, GmDeserializationOptions options) throws XMLStreamException {
		DeclaredArtifact artifact = null;
		StringBuilder commonBuilder = new StringBuilder();
		PomReadContext context = new PomReadContextImpl( commonBuilder, options.getSession());
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
			case XMLStreamConstants.START_ELEMENT : 
				String tag = reader.getName().getLocalPart();{
					if (tag.equalsIgnoreCase( PROJECT )) {
						artifact = ProjectExpert.read(context, reader);
					}
					break;
				}
			case XMLStreamConstants.END_ELEMENT : {
				return artifact;
			}
			case XMLStreamConstants.END_DOCUMENT: {
				return artifact;
			}
			default: 
				break;
			}
			reader.next();
		}	
		return artifact;
	}
	
	@Override
	public DeclaredArtifact unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		Maybe<DeclaredArtifact> maybe = unmarshallReasoned(in, options).cast();
		return maybe.get();
	}
	
	@Override
	public Maybe<Object> unmarshallReasoned(InputStream in, GmDeserializationOptions options) throws MarshallException {
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
	
	
}
