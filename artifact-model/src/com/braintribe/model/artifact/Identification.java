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
package com.braintribe.model.artifact;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * this is the base of the artifact family
 * @author pit
 *
 */

public interface Identification extends StandardIdentifiable {
	
	final EntityType<Identification> T = EntityTypes.T(Identification.class);
	
	public String getGroupId();
	public void setGroupId(String group);
	
	public String getArtifactId();
	public void setArtifactId(String id);
	
	public String getRevision();
	public void setRevision( String revision);
	
	public String getClassifier();
	public void setClassifier( String classifier);
}
