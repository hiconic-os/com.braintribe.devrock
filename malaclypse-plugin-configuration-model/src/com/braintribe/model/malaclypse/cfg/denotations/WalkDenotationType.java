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
