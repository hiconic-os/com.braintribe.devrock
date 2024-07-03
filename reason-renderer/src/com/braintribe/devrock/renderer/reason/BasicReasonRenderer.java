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
package com.braintribe.devrock.renderer.reason;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Reason;

public class BasicReasonRenderer implements ReasonRenderer {
	
	private Map<String, Supplier<String>> templateProviders;

	public BasicReasonRenderer(Map<String, Supplier<String>> templateProviders) {
		this.templateProviders = templateProviders;	
		
	}

	@Override
	public String render(Reason reason, String templateKey) {	
		return "not implemented yet";
	}

}
