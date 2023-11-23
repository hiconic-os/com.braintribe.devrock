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

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class ProjectExpert extends AbstractPomExpert implements HasPomTokens{

	public static Solution read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		Solution solution = Solution.T.create();
		// called at the start element event, must get passed this - because we SKIP anyother tag!!
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case GROUPID : {
							solution.setGroupId( extractString(context,  reader));
							break;
						}
						case ARTIFACTID: {
							solution.setArtifactId( extractString(context,  reader));
							break;
						}
						case VERSION: {
							String versionAsString = extractString(context,  reader);
							solution.setVersion( VersionProcessor.createFromString(versionAsString));
							break;
						}
						case PACKAGING: {
							solution.setPackaging( extractString(context, reader));
							break;
						}
						case PARENT : {
							solution.setParent( ParentExpert.read(context, reader));
							break;
						}
						case DEPENDENCIES : {
							solution.setDependencies(DependenciesExpert.read(context, reader));
							break;
						}
						case LICENSES: {
							solution.setLicenses( LicensesExpert.read(context, reader));
							break;
						}
						case DEPENDENCY_MANAGEMENT : {
							solution.setManagedDependencies( DependencyManagementExpert.read(context, reader));
							break;
						}
						case DISTRIBUTION_MANAGEMENT : {
							solution.setRedirection( DistributionManagementExpert.read(context, reader));
							break;
						}
						case PROPERTIES : {
							solution.setProperties( PropertiesExpert.read(context, reader));
							break;
						}
						default:
							skip(reader);
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT : {
					return solution;
				}
				default:
					break;
			}
			reader.next();
		}
		return null;
	}

}
