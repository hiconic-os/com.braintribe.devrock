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
package com.braintribe.devrock.repolet.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Dependency;
import com.braintribe.devrock.model.repolet.content.Property;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.resource.Resource;

/**
 * parses an expressive text file that defines a {@link RepoletContent}
 * @author pit
 *
 */
public class RepoletContentParser {
	public static RepoletContentParser INSTANCE = new RepoletContentParser();
	
	private static final int PARENT = 1;
	private static final int PROPERTY = 2;
	private static final int IMPORT = 3;
	private static final int DEPENDENCY = 4;
	private static final int PARTS = 5;
	private static final int SEQEND = 6;
	private static final int ARTIFACT = 7;
	private static final int DEPMGT = 8;
	private static final int REDIRECT = 9;
	private static final int VERSION_OVERRIDE = 10;

	private int determineCode( String line) {
		if (line.startsWith("-p")) {
			return PROPERTY;
		}
		if (line.startsWith("-r")) {
			return PARENT;
		}
		if (line.startsWith("-i")) {
			return IMPORT;
		}
		if (line.startsWith("-d")) {
			return DEPENDENCY;
		}
		if (line.startsWith("-c")) {
			return PARTS;
		}		
		if (line.startsWith("-x")) {
			return SEQEND;
		}		
		if (line.startsWith("-a")) {
			return ARTIFACT;
		}
		if (line.startsWith("-m")) {
			return DEPMGT;
		}	
		if (line.startsWith("-v")) {
			return VERSION_OVERRIDE;
		}	
		if (line.startsWith("-u")) {
			return REDIRECT;
		}	
		
		return -1;
	}
	
	static void transferVersionedArtifactIdentification( VersionedArtifactIdentification target, VersionedArtifactIdentification source) {
		target.setGroupId(source.getGroupId());
		target.setArtifactId( source.getArtifactId());
		target.setVersion( source.getVersion());
	}

	public RepoletContent parse( InputStream in) {
		RepoletContent repoContent = RepoletContent.T.create();
		int lastCode = -1;
		Artifact artifact = null;
		int numEmptyLines = 0;
		try ( BufferedReader reader = new BufferedReader( new InputStreamReader( in, "UTF-8"))) {				
			String line;
			while ((line = reader.readLine()) != null) 	{			
				try {			
					line = line.trim();
					if (line.length() == 0) {					
						if (++numEmptyLines >= 2) {
							lastCode = SEQEND;
							numEmptyLines = 0;
						}
						continue;
					}
					
					if (line.startsWith(";")) {
						continue;
					}
					
					if (line.startsWith( "@")) {
						String repoId = line.substring(1);
						repoContent.setRepositoryId(repoId);
						continue;
					}
					
					int code = determineCode(line);
					if (code > 0) {
						line = reader.readLine();
						if (line == null) {
							break;
						}
						line = line.trim();
						lastCode = code;
					}
					else {
						// no keyword.. either start of sequence or repeat  
						if (lastCode == SEQEND) {
							code = -1;
						}
						else {
							code = lastCode;
						}
					}
					
					
					switch (code) {
						case PROPERTY: {			
							int p = line.indexOf(':');
							String name = line.substring(0, p);
							String value = line.substring(p+1);
							Property property = Property.T.create();
							property.setName(name);
							property.setValue(value);
							artifact.getProperties().add( property);
							break;
						}							
						case PARENT:			
							artifact.setParent( VersionedArtifactIdentification.parse(line));
							break;
						case IMPORT:
							break;
						case DEPMGT:
							artifact.getManagedDependencies().add( Dependency.parse( line));
							break;
						case REDIRECT:
							artifact.setRedirection( Dependency.parse( line));
							break;
						case DEPENDENCY:				
							artifact.getDependencies().add( Dependency.parse( line));
							break;
						case PARTS:
							Pair<String,Resource> part = processPart( line);
							artifact.getParts().put( part.first, part.second);
							break;
						case VERSION_OVERRIDE:
							artifact.setVersionOverride( line);
							break;
						case SEQEND:
							break;						
						default: {
							artifact = Artifact.T.create();
							int pc = line.indexOf('|'); // check if packaging's set
							if (pc > 0) {								
								String remainder = line.substring(0, pc);
								int ac = remainder.indexOf('@');
								Integer order = null;
								if (ac > 0) {
									remainder = remainder.substring(0, ac);
									order = Integer.parseInt( remainder.substring( ac + 1));
									artifact.setOrder(order);
								}
								transferVersionedArtifactIdentification(artifact, VersionedArtifactIdentification.parse( remainder));
								artifact.setPackaging( line.substring( pc+1));
							}
							else {
								int ac = line.indexOf('@');
								Integer order = null;
								if (ac > 0) {
									String substring = line.substring( ac + 1);
									order = Integer.parseInt( substring);
									line = line.substring(0, ac);
									artifact.setOrder(order);
								}
								transferVersionedArtifactIdentification(artifact, VersionedArtifactIdentification.parse(line));
							}
							repoContent.getArtifacts().add( artifact);
						}
							
					}
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}
		catch( Exception e) {
			throw new IllegalStateException(e);
		}
		return repoContent;
	}

	public Pair<String,Resource> processPart(String line) {
		int p = line.indexOf(';');
		if (p < 0) {
			return Pair.of( line, null);							
		}
		else {
			String cl = line.substring(0, p);
			String res = line.substring(p+1);
			if (res.startsWith("@")) {
				return Pair.of( cl, ResourceParsers.parseFileResource( res.substring(1)));				
			}
			else {
				return Pair.of( cl, ResourceParsers.parse(res));				
			}			
		}		
	}
		
}
