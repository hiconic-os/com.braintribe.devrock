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
package com.braintribe.model.panther;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

public interface LogEntry extends GenericEntity {
	
	EntityType<LogEntry> T = EntityTypes.T(LogEntry.class);

	void setKind(String kind);
	String getKind();
	
	void setLevel(LogLevel level);
	LogLevel getLevel();
	
	void setMessage(String message);
	String getMessage();
	
	void setCapture(Resource resource);
	Resource getCapture();

	@Initializer("now()")
	Date getDate();
	void setDate(Date date);
	
}
