// ============================================================================
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
package com.braintribe.model.artifact.version;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface VersionRange extends StandardIdentifiable {

	final EntityType<VersionRange> T = EntityTypes.T(VersionRange.class);

	public Version getMinimum();
	public void setMinimum(Version version);

	public Version getMaximum();
	public void setMaximum(Version version);

	public Version getDirectMatch();
	public void setDirectMatch(Version version);

	public boolean getOpenLower();
	public void setOpenLower(boolean openLower);

	public boolean getOpenUpper();
	public void setOpenUpper(boolean openUpper);

	public boolean getInterval();
	public void setInterval(boolean noInterval);

	public boolean getUndefined();
	public void setUndefined(boolean undefined);

	public boolean getSymbolicLatest();
	public void setSymbolicLatest(boolean flag);

	public boolean getSymbolicRelease();
	public void setSymbolicRelease(boolean flag);

	public String getOriginalVersionRange();
	public void setOriginalVersionRange(String range);
	
	
	default Version lowerBound() {
		return getInterval()? getMinimum(): getDirectMatch();
	}
	
	default boolean lowerBoundOpen() {
		return getInterval()? getOpenLower(): false;
	}
	
	default Version upperBound() {
		return getInterval()? getMaximum(): getDirectMatch();
	}

	default boolean upperBoundOpen() {
		return getInterval()? getOpenUpper(): false;
	}
}
