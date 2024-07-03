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
