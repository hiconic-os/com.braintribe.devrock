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
package com.braintribe.model.artifact.essential;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a part, i.e. is a combination of classifier and type, aka PartTuple in 'mc the older's terms
 * 
 * @author pit/dirk
 *
 */
public interface PartIdentification extends GenericEntity {
	
	EntityType<PartIdentification> T = EntityTypes.T(PartIdentification.class);
	String classifier = "classifier";	
	String type = "type";
		
		
	/**
	 * @return - the classifier
	 */
	String getClassifier();
	void setClassifier( String classifier);
	
	/**
	 * @return - the type 
	 */
	String getType();
	void setType( String type);
	
	static PartIdentification from(PartIdentification pi) {
		return create(pi.getClassifier(), pi.getType());
	}
	
	/**
	 * creates a {@link PartIdentification} from the passed classifier and type 
	 * @param classifier - the classifier
	 * @param type - the type 
	 * @return - a fresh {@link PartIdentification}
	 */
	static PartIdentification create( String classifier, String type) {
		PartIdentification id = PartIdentification.T.create();
		id.setClassifier(classifier);
		id.setType(type);
		return id;
	}
	
	/**
	 * creates a {@link PartIdentification} from the passed type 
	 * @param type - the type of the part 
	 * @return - a fresh {@link PartIdentification}
	 * @deprecated use {@link #create(String)} instead
	 */
	@Deprecated
	static PartIdentification of( String type) {
		PartIdentification id = PartIdentification.T.create();	
		id.setType(type);
		return id;
	}
	
	/**
	 * creates a {@link PartIdentification} from the passed type 
	 * @param type - the type of the part 
	 * @return - a fresh {@link PartIdentification}
	 */
	static PartIdentification create( String type) {
		PartIdentification id = PartIdentification.T.create();	
		id.setType(type);
		return id;
	}
	
	/**
	 * @param expression - parses a valid expression in [<classifier>:]<type> format
	 * @return - a fresh {@link PartIdentification}
	 */
	static PartIdentification parse( String expression) {
		if (expression == null) {
			throw new IllegalArgumentException( "expression may not be null");
		}
		int colon = expression.indexOf( ':');
		if (colon <= 0) {
			return of( expression.substring( colon+1));
		}
		else {
			return create( expression.substring(0, colon), expression.substring(colon+1));
		}				
	}
	
	/**
	 * @return - a string representation of the {@link PartIdentification}
	 */
	default String asString() {
		return asString( this);
	}
	
	/**
	 * @param pi - the {@link PartIdentification} to output
	 * @return - the string representation of the {@link PartIdentification}
	 */
	static String asString(PartIdentification pi) {
		String t = pi.getType();
		
		if (t == null)
			t = "";
		
		if (pi.getClassifier() != null) {
			return pi.getClassifier() + ":" + t;
		}
		else {
			return ":" + t;
		}
	}
	
	default int compare( PartIdentification other) {
		if (other == null) {
			return 1;
		}
		int retval = 0;
		String myClassifier = getClassifier();
		String theirClassifier = other.getClassifier();
		if (myClassifier == null && theirClassifier != null) {
			return -1;
		}
		else if (myClassifier != null && theirClassifier == null) {
			return 1;
		}
		else if (myClassifier != null && theirClassifier != null) {
			retval = myClassifier.compareTo( theirClassifier);
		}				
		if (retval != 0)
			return retval;
		 
		String myType = getType();
		String theirType = other.getType();
		if (myType == null && theirType != null) {
			return -1;
		}
		else if (myType != null && theirType == null) {
			return 1;
		}
		else if (myType != null && theirType != null){
			return myType.compareTo( theirType);
		}		
		return 0;
	}	
}
