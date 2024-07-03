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
package com.braintribe.devrock.mc.core.compiled;

import java.util.function.Function;

import com.braintribe.gm.model.reason.Maybe;

public interface PropertyTransferBuilder {
	PropertyTransferBuilder targetProperty(String property);
	<F, T> PropertyTransferBuilder transformReasoned(Function<F, Maybe<T>> transformer);
	<F, T> PropertyTransferBuilder transform(Function<F, T> transformer);
	void build();
}
