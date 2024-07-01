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
package com.braintribe.gm.reason;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * ReasonBuilder is a fluent API that allows to build a reason with its text, causes, native exception and custom values. It allows to return the
 * constructed {@link Reason} directly or in form of a {@link Maybe} or to throw a {@link ReasonException} that carries the {@link Reason}
 * 
 * @author Dirk Scheffler
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reason)
public interface TemplateReasonBuilder<R extends Reason> {

	/** Typesafely assigns a value to the {@link Reason} via an {@link BiConsumer assigner} */
	<T> TemplateReasonBuilder<R> assign(BiConsumer<R, T> assigner, T value);

	/** Adds a {@link Reason} as cause to the property {@link Reason#getReasons()} */
	TemplateReasonBuilder<R> cause(Reason reason);

	/** Adds a number of {@link Reason reasons} as cause to the property {@link Reason#getReasons()} */
	TemplateReasonBuilder<R> causes(Reason... reasons);

	/** Adds a number of {@link Reason reasons} as cause to the property {@link Reason#getReasons()} */
	@JsIgnore
	TemplateReasonBuilder<R> causes(Collection<Reason> reasons);

	/** Allows a {@link Consumer} to enrich the constructed {@link Reason} with custom properties */
	TemplateReasonBuilder<R> enrich(Consumer<R> enricher);

	/** Returns a {@link Maybe} in failed state with the constructed {@link Reason} as {@link Maybe#whyUnsatisfied()} */
	<T> Maybe<T> toMaybe();

	/**
	 * Returns a {@link Maybe} in lenient failed state with the constructed {@link Reason} as {@link Maybe#whyUnsatisfied()} and the passed value as
	 * {@link Maybe#value()}
	 */
	@JsMethod(name = "toIncompleteMaybe")
	<T> Maybe<T> toMaybe(T value);

	/** Returns the constructed {@link Reason} */
	R toReason();
}