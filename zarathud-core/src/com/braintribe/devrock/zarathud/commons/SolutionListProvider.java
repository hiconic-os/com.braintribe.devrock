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
