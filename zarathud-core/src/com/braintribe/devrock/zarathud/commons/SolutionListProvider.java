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
package com.braintribe.devrock.zarathud.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.ProviderException;
import java.util.Collection;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.artifact.Solution;


public class SolutionListProvider implements Supplier<Collection<Solution>>{
	private File file;	
	private Collection<Solution> solutions;
	private static StaxMarshaller marshaller = new StaxMarshaller();
	
	public SolutionListProvider(File file) {
		this.file = file;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Solution> get() throws ProviderException {
		if (solutions != null)
			return solutions;		
		try (InputStream in = new FileInputStream( file)){
			solutions = (Collection<Solution>) marshaller.unmarshall( in);
		} catch (Exception e) {
			String msg = "cannot extract solutions from [" + file.getAbsolutePath() + "]";
			throw new ProviderException(msg, e);
		}
		
		return solutions;
	}

}
