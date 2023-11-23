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
