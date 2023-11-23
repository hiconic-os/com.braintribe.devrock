// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;

public class DependencyExpert extends AbstractPomExpert implements HasPomTokens {

	public static Dependency read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		reader.next();
		Dependency dependency = Dependency.T.create();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case GROUPID : {
							dependency.setGroupId( extractString(context, reader));
							break;
						}
						case ARTIFACTID: {
							dependency.setArtifactId( extractString(context,  reader));
							break;
						}
						case VERSION: {
							String versionAsString = extractString(context,  reader);
							dependency.setVersionRange( VersionRangeProcessor.createFromString(versionAsString));
							break;
						}
						case SCOPE: {
							dependency.setScope( extractString(context,  reader));
							break;
						}
						case CLASSIFIER : {
							dependency.setClassifier( extractString(context, reader));
							break;
						}
						case OPTIONAL: {
							dependency.setOptional( Boolean.parseBoolean( extractString(context, reader)));
							break;
						}
						case TYPE : {
							dependency.setType( extractString(context, reader));
							break;
						}
						case EXCLUSIONS : {
							dependency.setExclusions( ExclusionsExpert.read(context, reader));
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.PROCESSING_INSTRUCTION : {
					String piData = reader.getPIData().trim();
					switch ( reader.getPITarget()) {
						case PI_GROUP: {
							// direct group assignment 
							dependency.setGroup( piData);
							break;
						}
						case PI_ENRICH: {
							// manipulation enricher
							EnrichingExpert.read( dependency, piData);
							break;
						}
						case PI_PART : {
							// virtual part creation 
							VirtualPartExpert.read( dependency, piData);
							break;
						}
						case PI_TAG: {
							// tags
							dependency.getTags().add( piData);
							break;
						}
						case PI_REDIRECT: {
							// redirections 
							VirtualRedirectionExpert.read(dependency, piData);
							break;
						}
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return dependency;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}

}
