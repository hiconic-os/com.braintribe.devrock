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
package com.braintribe.devrock.mc.core.compiled;

import com.braintribe.devrock.mc.core.resolver.CachingCompiledArtifactAssocResolver;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;

/** This class is just there to make an expressive variant of CachingCompiledArtifactAssocResolver for {@link CompiledArtifact} that therefore implements {@link CompiledArtifactResolver} **/
public class CachingCompiledArtifactResolver extends CachingCompiledArtifactAssocResolver<CompiledArtifact> implements CompiledArtifactResolver {

}
