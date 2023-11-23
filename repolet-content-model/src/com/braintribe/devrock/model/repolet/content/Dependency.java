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
package com.braintribe.devrock.model.repolet.content;

import java.util.List;
import java.util.Map;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a dependency within the {@link RepoletContent}
 * @author pit
 *
 */
public interface Dependency extends VersionedArtifactIdentification {
	
	String scope = "scope";
	String type = "type";
	String classifier = "classifier";
	String exclusions = "exclusions";
	String processingInstructions = "processingInstructions";
	String optional = "optional";
	
	EntityType<Dependency> T = EntityTypes.T(Dependency.class);
	
	/**
	 * @return - the scope 
	 */
	String getScope();
	void setScope(String value);

	/**
	 * @return - the type 
	 */
	String getType();
	void setType(String value);
	
	/**
	 * @return - the classifier 
	 */
	String getClassifier();
	void setClassifier(String value);

	/**
	 * @return - exclusions 
	 */
	List<String> getExclusions();
	void setExclusions(List<String> value);
	
	/**
	 * @return - the 'optional' flag 
	 */
	boolean getOptional();
	void setOptional(boolean value);

	
	/**
	 * @return - processing instructions 
	 */
	Map<String,String> getProcessingInstructions();
	void setProcessingInstructions(Map<String,String> value);


	static void transferVersionedArtifactIdentification( VersionedArtifactIdentification target, VersionedArtifactIdentification source) {
		target.setGroupId(source.getGroupId());
		target.setArtifactId( source.getArtifactId());
		target.setVersion( source.getVersion());
	}

	static void transferVersionedArtifactIdentification( VersionedArtifactIdentification target, String potentiallyIncompleteDependencyDeclaration) {
		int gdel = potentiallyIncompleteDependencyDeclaration.indexOf( ':');
		int vdel = potentiallyIncompleteDependencyDeclaration.indexOf( "#");
		if (gdel > 0) {
			target.setGroupId( potentiallyIncompleteDependencyDeclaration.substring(0, gdel));		
		}
		if (vdel > 0) {
			if (gdel > 0) {
				target.setArtifactId( potentiallyIncompleteDependencyDeclaration.substring(gdel+1, vdel));				
			}
			else {
				target.setArtifactId( potentiallyIncompleteDependencyDeclaration.substring(0, vdel));
			}			
		}
		else {
			if (gdel > 0) {
				target.setArtifactId( potentiallyIncompleteDependencyDeclaration.substring(gdel+1));				
			}
			else {
				target.setArtifactId( potentiallyIncompleteDependencyDeclaration.substring(0));
			}
		}
		if (vdel > 0) {		
			target.setVersion( potentiallyIncompleteDependencyDeclaration.substring( vdel+1));
		}		
	}
	
	static Dependency create( VersionedArtifactIdentification source) {
		Dependency dependency = Dependency.T.create();
		transferVersionedArtifactIdentification(dependency, source);
		return dependency;
	}
	
	static Dependency parse(String str) {
		return parse( str, false);
	}

	/**
	 * the following format is supported : {@code [<groupId>]:<artifactId>#<version>[-<classifier>][:[scope]:[type]];[[<groupid>]:[<artifactId>],..]][|<pi-tag[:<pi-value>],[<pi-tag>[:<pi-value>],..]}
	 * @param str - the expression to parse
	 * @return - a {@link Dependency} built from it 
	 */
	static Dependency parse(String str, boolean lenient) {
		int posVersion = str.indexOf('#');	
		int posPi = str.indexOf( '|');
		
		String rstr = str;
		String processingInstructions = null;
		if (posPi > 0) {
			rstr = str.substring(0, posPi);
			processingInstructions = str.substring( posPi + 1);
		}
		
		int posScope = rstr.indexOf( ':', posVersion);		
		int posExclusion = rstr.indexOf( ";");
		int posPcVersion = rstr.indexOf( "-pc");
		int posClassifier;
		if (posPcVersion > 0) {
			posClassifier = rstr.indexOf('-', posPcVersion+1);
		}
		else {
			posClassifier = rstr.indexOf('-', posVersion);
		}
		if (posClassifier > posScope && posScope > 0) {
			posClassifier = -1;
		}
		
		String depDefinitionExpression = rstr;
		String exclusionExpression = null;
		if (posExclusion > 0) {
			depDefinitionExpression = rstr.substring(0, posExclusion);
			exclusionExpression = rstr.substring( posExclusion+1);
		}
		
		Dependency dependency = Dependency.T.create();
		if (posScope > 0) {		
			String identificationExpression = depDefinitionExpression.substring(0, posScope);
			String detailExpression = depDefinitionExpression.substring(posScope+1);
			
			if (posClassifier > 0) {
				dependency.setClassifier( identificationExpression.substring( posClassifier+1));
				identificationExpression = identificationExpression.substring(0, posClassifier);				
			}
			// check the expression if lenient
			if (!lenient) {
				transferVersionedArtifactIdentification(dependency, VersionedArtifactIdentification.parse( identificationExpression));
			}
			else {
				// TODO : build lenient code here 
				transferVersionedArtifactIdentification(dependency, VersionedArtifactIdentification.parse( identificationExpression));
			}
			int c2 = detailExpression.indexOf(':');
			if (c2 > 0) {
				dependency.setScope( detailExpression.substring(0, c2));
				String typeString = detailExpression.substring( c2+1);
				if (typeString.length() > 0)
					dependency.setType( typeString);
			}
			else {
				dependency.setType( detailExpression);
			}			
		}
		else {
			if (posClassifier > 0) {
				dependency.setClassifier( depDefinitionExpression.substring( posClassifier+1));
				depDefinitionExpression = depDefinitionExpression.substring(0, posClassifier);
			}
			// look at the expression if lenient
			if (!lenient) {
				transferVersionedArtifactIdentification( dependency, VersionedArtifactIdentification.parse( depDefinitionExpression));
			}
			else {				
				transferVersionedArtifactIdentification( dependency, VersionedArtifactIdentification.parse( depDefinitionExpression));
			}
			
		}
		if (exclusionExpression != null) {
			String [] excls = exclusionExpression.split(",");
			for (String excl : excls) {
				dependency.getExclusions().add( excl);				
			}						
		}
		
		if (processingInstructions != null) {
			String [] pis = processingInstructions.split(",");
			for (String pi : pis) {
				int pColon = pi.indexOf( ':');
				if (pColon < 0) {
					dependency.getProcessingInstructions().put( pi, "");
				}
				else {
					dependency.getProcessingInstructions().put( pi.substring(0, pColon), pi.substring(pColon+1));
				}				
			}
		}
		return dependency;
	}
	
}
