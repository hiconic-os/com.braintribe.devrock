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
package com.braintribe.model.malaclypse.cfg.preferences.mj;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;


public interface MungojerryPreferences extends StandardIdentifiable {
	
	final EntityType<MungojerryPreferences> T = EntityTypes.T(MungojerryPreferences.class);

	GwtPreferences getGwtPreferences();
	void setGwtPreferences( GwtPreferences gwtPreferences);
	
	MavenPreferences getMavenPreferences();
	void setMavenPreferences( MavenPreferences mvPreferences);
}
