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
package com.braintribe.gm.reason;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;

public class TemplateReasonBuilderImpl<R extends Reason> implements TemplateReasonBuilder<R> {
	private final R reason;
	
	public TemplateReasonBuilderImpl(EntityType<R> reasonType) {
		reason = reasonType.create();
	}
	
	@Override
	public <T> TemplateReasonBuilder<R> assign(BiConsumer<R, T> assigner, T value) {
		assigner.accept(reason, value);
		return this;
	}

	@Override
	public TemplateReasonBuilder<R> cause(Reason reason) {
		this.reason.getReasons().add(reason);
		return this;
	}

	@Override
	public TemplateReasonBuilder<R> causes(Reason... reasons) {
		return causes(Arrays.asList(reasons));
	}

	@Override
	public TemplateReasonBuilder<R> causes(Collection<Reason> reasons) {
		reason.getReasons().addAll(reasons);
		return this;
	}

	@Override
	public R toReason() {
		if (reason.getText() == null) {
			String text = TemplateReasons.buildPlainText(reason);
			reason.setText(text);
		}
		return reason;
	}
	
	@Override
	public <T> Maybe<T> toMaybe() {
		return Maybe.empty(toReason());
	}
	
	@Override
	public <T> Maybe<T> toMaybe(T value) {
		return Maybe.incomplete(value, toReason());
	}

	@Override
	public TemplateReasonBuilder<R> enrich(Consumer<R> enricher) {
		enricher.accept(reason);
		return this;
	}
	
	

}
