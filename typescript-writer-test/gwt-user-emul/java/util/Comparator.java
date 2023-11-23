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

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

import java.JsAnnotationsPackageNames;


/**
 * An interface used a basis for implementing custom ordering. <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/Comparator.html">[Sun
 * docs]</a>
 *
 * @param <T> the type to be compared.
 */
@FunctionalInterface
@JsType(namespace = JsAnnotationsPackageNames.JAVA_UTIL)
@SuppressWarnings("unusable-by-js")
public interface Comparator<T> {

  int compare(T a, T b);

  @Override
  boolean equals(Object other);

	  default Comparator<T> reversed() {
		    return null;
	  }

  default Comparator<T> thenComparing(Comparator<? super T> other) {
    checkNotNull(other);
    return (Comparator<T> & Serializable) (a, b) -> {
      int c = compare(a, b);
      return (c != 0) ? c : other.compare(a, b);
    };
  }

  @JsMethod(name="thenComparingByWith")
  default <U> Comparator<T> thenComparing(Function<? super T, ? extends U> keyExtractor,
      Comparator<? super U> keyComparator) {
    return thenComparing(comparing(keyExtractor, keyComparator));
  }

  @JsMethod(name="thenComparingBy")
  default <U extends Comparable<? super U>> Comparator<T> thenComparing(
      Function<? super T, ? extends U> keyExtractor) {
    return thenComparing(comparing(keyExtractor));
  }

  @JsIgnore
  default Comparator<T> thenComparingInt(ToIntFunction<? super T> keyExtractor) {
    return thenComparing(comparingInt(keyExtractor));
  }

  @JsIgnore
  default Comparator<T> thenComparingLong(ToLongFunction<? super T> keyExtractor) {
    return thenComparing(comparingLong(keyExtractor));
  }

  @JsIgnore
  default Comparator<T> thenComparingDouble(ToDoubleFunction<? super T> keyExtractor) {
    return thenComparing(comparingDouble(keyExtractor));
  }

  @JsMethod(name="comparingWith")
  static <T, U> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor,
      Comparator<? super U> keyComparator) {
    checkNotNull(keyExtractor);
    checkNotNull(keyComparator);
    return (Comparator<T> & Serializable) (a, b) ->
        keyComparator.compare(keyExtractor.apply(a), keyExtractor.apply(b));
  }

  static <T, U extends Comparable<? super U>> Comparator<T> comparing(
      Function<? super T, ? extends U> keyExtractor) {
    return comparing(keyExtractor, naturalOrder());
  }

  @JsIgnore
  static<T> Comparator<T> comparingDouble(ToDoubleFunction<? super T> keyExtractor) {
    checkNotNull(keyExtractor);
    return (Comparator<T> & Serializable) (a, b) ->
        Double.compare(keyExtractor.applyAsDouble(a), keyExtractor.applyAsDouble(b));
  }

  @JsIgnore
  static <T> Comparator<T> comparingInt(ToIntFunction<? super T> keyExtractor) {
    checkNotNull(keyExtractor);
    return (Comparator<T> & Serializable) (a, b) ->
        Integer.compare(keyExtractor.applyAsInt(a), keyExtractor.applyAsInt(b));
  }

  @JsIgnore
  static <T> Comparator<T> comparingLong(ToLongFunction<? super T> keyExtractor) {
    checkNotNull(keyExtractor);
    return (Comparator<T> & Serializable) (a, b) ->
        Long.compare(keyExtractor.applyAsLong(a), keyExtractor.applyAsLong(b));
  }

	  static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
		    return null;
	  }

	  static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
		    return null;
	  }

	  static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
		    return null;
	  }

	  static <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
	    return null;
	  }
	}
