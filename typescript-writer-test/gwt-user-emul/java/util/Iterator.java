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
package java.util;

import static javaemul.internal.InternalPreconditions.checkNotNull;

import java.JsAnnotationsPackageNames;
import java.util.function.Consumer;

import jsinterop.annotations.JsType;

/**
 * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html">
 * the official Java API doc</a> for details.
 *
 * @param <E> element type
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_UTIL)
@SuppressWarnings("unusable-by-js")
public interface Iterator<E> {

  boolean hasNext();

  E next();

  default void forEachRemaining(Consumer<? super E> consumer) {
    checkNotNull(consumer);
    while (hasNext()) {
      consumer.accept(next());
    }
  }

  default void remove() {
    throw new UnsupportedOperationException();
  }
}
