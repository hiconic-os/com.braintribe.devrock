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
package com.braintribe.devrock.model.mc.core.event;

import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OnPartDownloading extends GenericEntity {
	EntityType<OnPartDownloading> T = EntityTypes.T(OnPartDownloading.class);
	
	static String part = "part";
	static String repositoryOrigin = "repositoryOrigin";
	static String resource = "resource";

	CompiledPartIdentification getPart();
	void setPart(CompiledPartIdentification part);
	
	String getRepositoryOrigin();
	void setRepositoryOrigin(String repositoryOrigin);
	
	int getDataAmount();
	void setDataAmount(int dataAmount);
	
	int getTotalDataAmount();
	void setTotalDataAmount(int totalDataAmount);

}
