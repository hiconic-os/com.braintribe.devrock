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
package com.braintribe.devrock.mc.core.resolver.js;

import java.util.LinkedList;
import java.util.List;

import com.braintribe.exception.Exceptions;

/**
 * simple exception collector - collects exceptions, wraps them, and throws a single wrapper exception 
 * @author pit
 *
 */
public class ExceptionCollector {	
	private List<Throwable> exceptions;
	private String message;
	
	/**
	 * @param message - the 'main' message of the {@link ExceptionCollector}
	 */
	public ExceptionCollector(String message) {
		super();
		this.message = message;
	}

	/**
	 * @param e - a {@link Throwable} to add to the collection
	 */
	public void collect(Throwable e) {
		if (exceptions == null)
			exceptions = new LinkedList<>();
		
		exceptions.add(e);
	}
	
	/**
	 * if there are stored exception, a {@link RuntimeException} is thrown with the exceptions collected
	 * added as suppressed exceptions
	 */
	public void throwIfNotEmpty() {
		int size = exceptions != null? exceptions.size(): 0;
		
		switch (size) {
		case 0:
			return;
		case 1:
			throw Exceptions.unchecked(exceptions.get(0), message);
		default:
			RuntimeException e = new RuntimeException(message);
			exceptions.forEach(e::addSuppressed);
			throw e;
		}
	}
}
