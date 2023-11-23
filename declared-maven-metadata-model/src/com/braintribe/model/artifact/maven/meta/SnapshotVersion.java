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
package com.braintribe.model.artifact.maven.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a snapshot version representation 
 * @author pit
 *
 */
public interface SnapshotVersion extends GenericEntity{
	
	final EntityType<SnapshotVersion> T = EntityTypes.T(SnapshotVersion.class);
	
	/**
	 * @return - deployed time stamp
	 */
	String getUpdated();
	void setUpdated( String updated);
	
	String getClassifier();
	void setClassifier( String classifier);
	
	String getExtension();
	void setExtension( String extension);
	
	String getValue();
	void setValue(String value);
	
}
