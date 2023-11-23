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
package com.braintribe.devrock.mc.core.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.service.api.result.Neutral;

public class MaybeInputStreamProvider implements InputStreamProvider {
	private Supplier<Maybe<InputStream>> supplier;
	
	private InputStream in;
	
	public MaybeInputStreamProvider(Supplier<Maybe<InputStream>> supplier) {
		super();
		this.supplier = supplier;
	}

	public Maybe<Neutral> isAvailable() {
		if (in != null)
			return Maybe.complete(Neutral.NEUTRAL);
		
		Maybe<InputStream> maybe = supplier.get();
		
		if (maybe.isSatisfied()) {
			in = maybe.get();
			return Maybe.complete(Neutral.NEUTRAL);
		}
		else {
			return maybe.emptyCast();
		}
	}

	@Override
	public InputStream openInputStream() throws IOException {
		if (in != null) {
			InputStream retVal = in;
			in = null;
			return retVal;
		}
		
		return supplier.get().get();
	}

}
