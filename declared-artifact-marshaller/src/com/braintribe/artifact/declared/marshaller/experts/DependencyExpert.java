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
