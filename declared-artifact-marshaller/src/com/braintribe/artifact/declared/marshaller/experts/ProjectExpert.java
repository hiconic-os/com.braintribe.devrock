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
package com.braintribe.artifact.declared.marshaller.experts;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.artifact.declared.DeclaredArtifact;

public class ProjectExpert extends AbstractPomExpert implements HasPomTokens{

	public static DeclaredArtifact read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		DeclaredArtifact solution = create( context, DeclaredArtifact.T);
		context.setOrigin( solution);
		// called at the start element event, must get passed this - because we SKIP anyother tag!!
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {	
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case NAME : {
							solution.setName( extractString(context,  reader));
							break;
						}
						case DESCRIPTION : {
							solution.setDescription( extractString(context,  reader));
							break;
						}
						case GROUPID : {
							if (solution.getGroupId() != null) {
								throw new XMLStreamException("duplicate tag [" + GROUPID + "]");							 
							}
							solution.setGroupId( extractString(context,  reader));
							break;
						}
						case ARTIFACTID: {
							if (solution.getArtifactId() != null) {
								throw new XMLStreamException("duplicate tag [" + ARTIFACTID + "]");
							}
							solution.setArtifactId( extractString(context,  reader));
							break;
						}						
						case VERSION: {
							if (solution.getVersion() != null) {
								throw new XMLStreamException("duplicate tag [" + VERSION + "]");
							}
							solution.setVersion(  extractString(context,  reader));
							break;
						}
						case PACKAGING: {
							if (solution.getPackaging() != null) { 
								throw new XMLStreamException("duplicate tag [" + PACKAGING + "]");
							}
							solution.setPackaging( extractString(context, reader));
							break;
						}
						case PARENT : {
							if (solution.getParentReference() != null) {
								throw new XMLStreamException("duplicate tag [" + PARENT + "]");
							}
							solution.setParentReference( ParentExpert.read(context, reader));
							break;
						}
						case DEPENDENCIES : {
							if (solution.getDependencies() != null && !solution.getDependencies().isEmpty()) {
								throw new XMLStreamException("duplicate tag [" + DEPENDENCIES + "]");
							}
							solution.setDependencies(DependenciesExpert.read(context, reader));
							break;
						}
						case LICENSES: {
							if (solution.getLicenses() != null && !solution.getLicenses().isEmpty()) {
								throw new XMLStreamException("duplicate tag [" + LICENSES + "]");
							}
							solution.setLicenses( LicensesExpert.read(context, reader));
							break;
						}
						case DEPENDENCY_MANAGEMENT : {
							if (solution.getManagedDependencies() != null && !solution.getManagedDependencies().isEmpty()) {
								throw new XMLStreamException("duplicate tag [" + DEPENDENCY_MANAGEMENT + "]");
							}
							solution.setManagedDependencies( DependencyManagementExpert.read(context, reader));
							break;
						}
						case DISTRIBUTION_MANAGEMENT : {
							if (solution.getDistributionManagement() != null) {
								throw new XMLStreamException("duplicate tag [" + DISTRIBUTION_MANAGEMENT + "]");
							}
							solution.setDistributionManagement( DistributionManagementExpert.read(context, reader));
							break;
						}
						case PROPERTIES : {
							if (solution.getProperties() != null && !solution.getProperties().isEmpty()) {
								throw new XMLStreamException("duplicate tag [" + PROPERTIES + "]");
							}
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
