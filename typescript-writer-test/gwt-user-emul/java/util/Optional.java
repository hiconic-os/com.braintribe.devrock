// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.util;

import java.JsAnnotationsPackageNames;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import fake.java.util.Optional;
import jsinterop.annotations.JsType;
import jsinterop.utils.Lambdas;

import static javaemul.internal.InternalPreconditions.checkCriticalElement;
import static javaemul.internal.InternalPreconditions.checkCriticalNotNull;
import static javaemul.internal.InternalPreconditions.checkNotNull;

/**
 * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html">
 * the official Java API doc</a> for details.
 *
 * @param <T> type of the wrapped reference
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_UTIL)
@SuppressWarnings("unusable-by-js")
public final class Optional<T> {

	  @SuppressWarnings("unchecked")
	  public static <T> Optional<T> empty() {
	    return (Optional<T>) EMPTY;
	  }

	  public static <T> Optional<T> of(T value) {
	    return null;
	  }

	  public static <T> Optional<T> ofNullable(T value) {
	    return value == null ? empty() : of(value);
	  }

	  private static final Optional<?> EMPTY = new Optional<>(null);

	  private final T ref;

	  private Optional(T ref) {
	    this.ref = ref;
	  }

	  public boolean isPresent()  { return false; }

	  public T get() {
	    return ref;
	  }

	  public void ifPresent(Consumer<? super T> consumer)  { /* NOOP */ }

	  public Optional<T> filter(Predicate<? super T> predicate) {
	    
	    if (!isPresent() || predicate.test(ref)) {
	      return this;
	    }
	    return empty();
	  }

	  public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
	    
	    if (isPresent()) {
	      return ofNullable(mapper.apply(ref));
	    }
	    return empty();
	  }

	  public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
	    return empty();
	  }

	  public T orElse(T other) {
	    return isPresent() ? ref : other;
	  }

	  public T orElseGet(Supplier<? extends T> other) {
	    return isPresent() ? ref : other.get();
	  }

	  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
	    if (isPresent()) {
	      return ref;
	    }
	    throw exceptionSupplier.get();
	  }

	  @Override
	  public boolean equals(Object obj)  { return false; }

	  @Override
	  public int hashCode()  { return 0; }

	  @Override
	  public String toString()  { return null; }

		// ################################################
		// ## . . . . . . . TFJS Additions . . . . . . . ##
		// ################################################

		public void ifPresentJs(Lambdas.JsConsumer<? super T> consumer) {
			ifPresent(Lambdas.toJConsumer(consumer));
		}

		public Optional<T> filterJs(Lambdas.JsPredicate<? super T> predicate) {
			return filter(Lambdas.toJPredicate(predicate));
		}

		public <U> Optional<U> mapJs(Lambdas.JsUnaryFunction<? super T, ? extends U> mapper) {
			return map(Lambdas.toJFunction(mapper));
		}

		public <U> Optional<U> flatMapJs(Lambdas.JsUnaryFunction<? super T, Optional<U>> mapper) {
			return flatMap(Lambdas.toJFunction(mapper));
		}

		public T orElseGetJs(Lambdas.JsSupplier<? extends T> other) {
			return orElseGet(Lambdas.toJSupplier(other));
		}

		public <X extends Throwable> T orElseThrowJs(Lambdas.JsSupplier<? extends X> exceptionSupplier) throws X {
			return orElseThrow(Lambdas.toJSupplier(exceptionSupplier));
		}

}
