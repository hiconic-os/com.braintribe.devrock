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
			inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false); // This disables DTDs entirely for that factory
			
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
