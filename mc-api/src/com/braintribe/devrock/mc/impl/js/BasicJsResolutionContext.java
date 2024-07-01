// ============================================================================
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
package com.braintribe.devrock.mc.impl.js;

import com.braintribe.devrock.mc.api.js.JsResolutionContext;
import com.braintribe.devrock.mc.api.js.JsResolutionContextBuilder;
import com.braintribe.devrock.mc.api.js.JsResolutionListener;
import com.braintribe.devrock.mc.api.js.NormalizedJsEnrichment;

public class BasicJsResolutionContext implements JsResolutionContextBuilder, JsResolutionContext {

	private boolean enrichMin = false;
	private boolean enrichPretty = false;
	private boolean lenient = false;
	private NormalizedJsEnrichment normalizedJsEnrichment = NormalizedJsEnrichment.none;
	private boolean includeAggregatorsInSolutions;
	private JsResolutionListener listener = JsResolutionListener.EMPTY;

	@Override
	public boolean lenient() {
		return lenient;
	}

	@Override
	public boolean enrichMin() {
		return enrichMin;
	}

	@Override
	public boolean enrichPretty() {
		return enrichPretty;
	}

	@Override
	public NormalizedJsEnrichment enrichNormalized() {
		return normalizedJsEnrichment ;
	}
	
	@Override
	public boolean includeAggregatorsInSolutions() {
		return includeAggregatorsInSolutions;
	}
	
	@Override
	public JsResolutionListener listener() {
		return listener;
	}

	@Override
	public JsResolutionContextBuilder enrichMin(boolean enrichMin) {
		this.enrichMin = enrichMin;
		return this;
	}

	@Override
	public JsResolutionContextBuilder enrichPretty(boolean enrichPretty) {
		this.enrichPretty = enrichPretty;
		return this;
	}

	@Override
	public JsResolutionContextBuilder enrichmentNormalized(NormalizedJsEnrichment normalizedJsEnrichment) {
		this.normalizedJsEnrichment = normalizedJsEnrichment;
		return this;
	}

	@Override
	public JsResolutionContextBuilder lenient(boolean lenient) {
		this.lenient = lenient;
		return this;
	}
	
	@Override
	public JsResolutionContextBuilder includeAggregatorsInSolutions(boolean includeAggregatorsInSolutions) {
		this.includeAggregatorsInSolutions = includeAggregatorsInSolutions;
		return this;
	}
	
	@Override
	public JsResolutionContextBuilder listener(JsResolutionListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	public JsResolutionContext done() {
		return this;
	}

}
