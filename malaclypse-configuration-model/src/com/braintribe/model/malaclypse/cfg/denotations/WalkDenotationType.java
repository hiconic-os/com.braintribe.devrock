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
package com.braintribe.model.malaclypse.cfg.denotations;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ClashResolverDenotationType;


public interface WalkDenotationType extends GenericEntity{
	
	final EntityType<WalkDenotationType> T = EntityTypes.T(WalkDenotationType.class);

	ClashResolverDenotationType getClashResolverDenotationType();
	void setClashResolverDenotationType( ClashResolverDenotationType type);
	
	ScopeControlDenotationType getScopeControlDenotationType();
	void setScopeControlDenotationType( ScopeControlDenotationType type);

	ExclusionControlDenotationType getExclusionControlDenotationType();
	void setExclusionControlDenotationType( ExclusionControlDenotationType type);
	
	WalkDomain getWalkDomain();
	void setWalkDomain( WalkDomain walkDomain);
	
	WalkKind getWalkKind();
	void setWalkKind( WalkKind walkKind);
		
	String getTagRule();
	void setTagRule( String tagRule);
	
	String getTypeFilter();
	void setTypeFilter( String typeFilter);
	
}
