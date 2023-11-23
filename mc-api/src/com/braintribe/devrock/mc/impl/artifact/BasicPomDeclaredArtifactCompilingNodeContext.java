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
