// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/*
 * Copyright 2007 Google Inc.
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
package java.lang;

import static javaemul.internal.InternalPreconditions.checkNotNull;

import java.JsAnnotationsPackageNames;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.utils.Lambdas;
import jsinterop.utils.Lambdas.JsConsumer;

/**
 * Allows an instance of a class implementing this interface to be used in the foreach statement. See
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html"> the official Java API doc</a> for details.
 *
 * @param <T>
 *            type of returned iterator
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_LANG)
@SuppressWarnings("unusable-by-js")
public interface Iterable<T> {
  Iterator<T> iterator();

  @JsMethod(name = "each")
  default void forEach(Consumer<? super T> action) {
    checkNotNull(action);
    for (T t : this) {
      action.accept(t);
    }
  }

  @JsIgnore
  default Spliterator<T> spliterator() {
    return Spliterators.spliteratorUnknownSize(iterator(), 0);
  }

	// ################################################
	// ## . . . . . . . TFJS Additions . . . . . . . ##
	// ################################################

	@JsMethod(name = "forEach")
	default void forEachJs(JsConsumer<? super T> consumer) {
		checkNotNull(consumer);
		forEach(Lambdas.toJConsumer(consumer));
	}

	/** Returns a native java iterable whose implementation is based on the iterator. */
	default JsIterable<T> iterable() {
		return JsIterableHelper.iterable(this);
	}

}
