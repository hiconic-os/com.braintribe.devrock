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
package com.braintribe.test.multi.framework;

import java.util.function.Function;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClient;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.test.multi.framework.fake.direct.DirectRepositoryAccessClient;
import com.braintribe.test.multi.framework.fake.direct.DirectSnapshotRepositoryAccessClient;

public class FakeRepositoryAccessClientFactory extends AbstractFakeClientFactoryBase implements Function<String, RepositoryAccessClient> {

	private Function<String, RepositoryAccessClient> delegate;	
	@Configurable @Required
	public void setDelegate(Function<String, RepositoryAccessClient> delegate) {
		this.delegate = delegate;
	}
	
	
	@Override
	public RepositoryAccessClient apply(String index) throws RuntimeException {
		
		String [] fakeContent = getContentsForKey( index);
		if (fakeContent != null) {
			return new DirectRepositoryAccessClient(index, getExpansive(index), fakeContent);
		}
		SnapshotTuple [] tuples = getTuplesForKey(index);
		if (tuples != null) {
			return new DirectSnapshotRepositoryAccessClient(index, getExpansive(index), tuples);
		}		
		return delegate.apply(index);
		
	}

	

}
