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

import java.util.ArrayList;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.ProcessingInstruction;

public class DependencyExpert extends AbstractPomExpert implements HasPomTokens {
	private static boolean transposeCommentToPi = false;

	public static DeclaredDependency read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException {
		reader.next();
		DeclaredDependency dependency = create( context, DeclaredDependency.T);
		dependency.setOrigin(context.getOrigin());
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
							dependency.setVersion( extractString(context, reader));
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
				case XMLStreamConstants.COMMENT: {
					// just to be able to switch that off for now 
					if (!transposeCommentToPi)
						break;
					
					boolean hasText = reader.hasText();
					if (hasText) {
						String comment = reader.getText().trim();
						ProcessingInstruction pi = TagResolver.fromComment( comment, () -> create( context, ProcessingInstruction.T));
						if (dependency.getProcessingInstructions() == null) {
							dependency.setProcessingInstructions( new ArrayList<>());
						}					
						dependency.getProcessingInstructions().add( pi);					
					}
					/*
					boolean hasText = reader.hasText();
					if (hasText) {
						String comment = reader.getText().trim();
						int c = comment.indexOf( ':');
						String target = null;
						String data = null;
						if (c < 0) {			
							for (int i = 0; i < comment.length(); i++) {
								char ch = comment.charAt( i);
								if (Character.isWhitespace(ch)) {
									target = comment.substring(0, i);
									data = comment.substring(i+1);
									break;
								}
							}
						
						}
						else {
							target = comment.substring(0, c);
							data = comment.substring(c+1);						
						}
						
						ProcessingInstruction pi = create( context, ProcessingInstruction.T);						
						pi.setTarget( target.trim()); // just drop whitespace arround the tag and it's ':'
						pi.setData( data.trim()); // data is just the remainder
						
						if (dependency.getProcessingInstructions() == null) {
							dependency.setProcessingInstructions( new ArrayList<>());
						}
						dependency.getProcessingInstructions().add( pi);						
					}
					*/
					break;
				}
				case XMLStreamConstants.PROCESSING_INSTRUCTION : {
					ProcessingInstruction pi = TagResolver.fromProcessingInstruction( reader.getPITarget(), reader.getPIData(), () -> create( context, ProcessingInstruction.T));
					if (dependency.getProcessingInstructions() == null) {
						dependency.setProcessingInstructions( new ArrayList<>());
					}					
					dependency.getProcessingInstructions().add( pi);
					/*
					ProcessingInstruction pi = create( context, ProcessingInstruction.T);
					pi.setTarget(reader.getPITarget());
					pi.setData( reader.getPIData().trim());
					if (dependency.getProcessingInstructions() == null) {
						dependency.setProcessingInstructions( new ArrayList<>());
					}
					*/
					//dependency.getProcessingInstructions().add( pi);															
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
