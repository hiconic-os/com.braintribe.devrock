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
package com.braintribe.devrock.zarathud.commons;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Identification;

/**
 * wrapper codec for the {@link CodingSet} or {@link CodingMap} based structures with {@link Identification} 
 * @author Pit
 *
 */
public class IdentificationWrapperCodec extends HashSupportWrapperCodec<Identification> {
	
	public IdentificationWrapperCodec() {
		super(true);
	}

	@Override
	protected int entityHashCode(Identification e) {
		return (e.getGroupId() + ":" + e.getArtifactId()).hashCode();
	}

	@Override
	protected boolean entityEquals(Identification e1, Identification e2) {
		if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
			return false;
		if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
			return false;
		return true;
	}

}
