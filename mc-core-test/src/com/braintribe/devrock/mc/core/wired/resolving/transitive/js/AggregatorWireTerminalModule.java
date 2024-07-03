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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.js;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.space.WireSpace;

public class AggregatorWireTerminalModule<T extends WireSpace> implements WireTerminalModule<T> {
	private List<WireModule> dependencies = new ArrayList<>();
	private Class<T> contract;
	
	public AggregatorWireTerminalModule(WireTerminalModule<T> mainModule) {
		dependencies.add(mainModule);
		contract = mainModule.contract();
	}
	
	public void addModule(WireModule module) {
		dependencies.add(module);
	}
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		// noop
	}
	
	@Override
	public List<WireModule> dependencies() {
		return dependencies;
	}
	
	@Override
	public Class<T> contract() {
		return contract;
	}
}
