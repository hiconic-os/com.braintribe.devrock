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

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * currently, two different types 
 * - classpath : standard solution enrichment, no special order
 * - buildOrder: no enrichment, yet builder order order (so it can be built by a process), *not implemented yet*
 * @author Pit
 *
 */
public enum WalkKind implements EnumBase {
	classpath, buildOrder, hierarchy;
	
	
	final EnumType T = EnumTypes.T(WalkKind.class);

	@Override
	public EnumType type() {
		return T;
	}
}
