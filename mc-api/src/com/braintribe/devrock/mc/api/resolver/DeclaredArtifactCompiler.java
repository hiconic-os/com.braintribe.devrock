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
