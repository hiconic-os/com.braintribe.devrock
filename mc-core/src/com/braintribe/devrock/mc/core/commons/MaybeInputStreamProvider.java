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
