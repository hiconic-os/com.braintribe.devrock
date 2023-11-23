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
package com.braintribe.devrock.mc.api.resolver;

import java.io.File;
import java.io.InputStream;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.declared.DeclaredArtifact;

/**
 * interface for the 'pom compiling' feature
 * 
 * @author pit / dirk
 *
 */
public interface DeclaredArtifactCompiler {

	/**
	 * compiles a pom while reading the contents from the inputstream
	 * @param in - the {@link InputStream}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 * @deprecated use {@link #compile(DeclaredArtifact)}
	 */
	@Deprecated
	default CompiledArtifact compile(InputStream in) {
		return compileReasoned(in).get();
	}

	@Deprecated
	/**
	 * compiles a pom while reading the contents from the file
	 * @param in - the {@link File}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 */
	default CompiledArtifact compile(File file) {
		return compileReasoned(file).get();
	}
	
	@Deprecated
	/**
	 * compiles a pom from its modelled representation 
	 * @param in - the {@link DeclaredArtifact}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 */
	default CompiledArtifact compile(DeclaredArtifact declaredArtifact) {
		return compileReasoned(declaredArtifact).get();
	}
	
	/**
	 * compiles a pom while reading the contents from the inputstream
	 * @param in - the {@link InputStream}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 */
	Maybe<CompiledArtifact> compileReasoned(InputStream in);
	/**
	 * compiles a pom while reading the contents from the file
	 * @param in - the {@link File}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 */
	Maybe<CompiledArtifact> compileReasoned(File file);
	/**
	 * compiles a pom from its modelled representation 
	 * @param in - the {@link DeclaredArtifact}
	 * @return - the {@link CompiledArtifact}, which may be invalid, check resulting {@link CompiledArtifact}
	 */
	Maybe<CompiledArtifact> compileReasoned(DeclaredArtifact declaredArtifact);
}
