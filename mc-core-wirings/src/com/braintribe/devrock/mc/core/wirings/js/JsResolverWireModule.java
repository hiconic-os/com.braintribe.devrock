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
package com.braintribe.devrock.mc.core.wirings.js;

import java.util.List;

import com.braintribe.devrock.mc.core.resolver.js.BasicJsDependencyResolver;
import com.braintribe.devrock.mc.core.resolver.js.BasicJsLibraryLinker;
import com.braintribe.devrock.mc.core.wirings.js.contract.JsResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * This {@link WireTerminalModule} configures a {@link BasicJsDependencyResolver} and {@link BasicJsLibraryLinker} along
 * with their precursors to be functional for transitive classpath Java dependency resolution.
 * 
 * @author Dirk Scheffler
 *
 */
public enum JsResolverWireModule implements WireTerminalModule<JsResolverContract> {
	INSTANCE;
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list(TransitiveResolverWireModule.INSTANCE);
	}
}
