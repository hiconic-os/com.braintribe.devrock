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
package com.braintribe.devrock.mc.api.commons;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * 
 * @author pit / dirk
 *
 */
public interface Functions {
	static <T> Predicate<T> invariantTrue() {
		return c -> true;
	}
	
	static <T> Predicate<T> invariantFalse() {
		return c -> false;
	}
	
	static <T,X> BiPredicate<T,X> negativeBiPredicate() {
		return (a,b) -> false;
	}
	
	static <T,X> BiPredicate<T,X> positiveBiPredicate() {
		return (a,b) -> true;
	}
}
