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
package com.braintribe.devrock.mc.impl.artifact;

import com.braintribe.devrock.mc.api.compiled.DeclaredArtifactCompilingNodeContext;
import com.braintribe.devrock.mc.api.compiled.DeclaredArtifactCompilingNodeContextBuilder;

/**
 * @author pit
 *
 */
public class BasicPomDeclaredArtifactCompilingNodeContext implements DeclaredArtifactCompilingNodeContext, DeclaredArtifactCompilingNodeContextBuilder {
	boolean artifactLeniency;
	boolean dependencyLeniency;
	boolean parentLeniency;
	boolean importLeniency;

	@Override
	public DeclaredArtifactCompilingNodeContextBuilder artifactLeniceny(boolean leniency) {
		artifactLeniency = leniency;
		return this;
	}

	@Override
	public DeclaredArtifactCompilingNodeContextBuilder dependencyLeniceny(boolean leniency) {
		dependencyLeniency = leniency;
		return this;
	}

	@Override
	public DeclaredArtifactCompilingNodeContextBuilder parentLeniceny(boolean leniency) {
		parentLeniency = leniency;
		return this;
	}

	@Override
	public DeclaredArtifactCompilingNodeContextBuilder importLeniceny(boolean leniency) {
		importLeniency = leniency;
		return this;
	}

	@Override
	public DeclaredArtifactCompilingNodeContext done() {
		return this;
	}
	
	@Override
	public DeclaredArtifactCompilingNodeContextBuilder leniceny(boolean leniency) {
		artifactLeniency = leniency;
		dependencyLeniency = leniency;
		parentLeniency = leniency;
		importLeniency = leniency;
		return this;
	}

	@Override
	public boolean artifactLeniency() {	
		return artifactLeniency;
	}

	@Override
	public boolean dependencyLeniency() {
		return dependencyLeniency;
	}

	@Override
	public boolean parentLeniency() {
		return parentLeniency;
	}

	@Override
	public boolean importLeniency() {
		return importLeniency;
	}


}
