// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/*
 * Copyright 2016 Google Inc.
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

package java.util.stream;

import static javaemul.internal.InternalPreconditions.checkNotNull;
import static javaemul.internal.InternalPreconditions.checkState;

import java.JsAnnotationsPackageNames;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractDoubleSpliterator;
import java.util.Spliterators.AbstractIntSpliterator;
import java.util.Spliterators.AbstractLongSpliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import fake.java.util.stream.BaseStream;
import fake.java.util.stream.Collector;
import fake.java.util.stream.Stream;
import fake.java.lang.JsIterable;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.utils.Lambdas;

/**
 * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html">
 * the official Java API doc</a> for details.
 *
 * @param <T> the type of data being streamed
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_UTIL)
@SuppressWarnings("unusable-by-js")
public interface Stream<T> extends BaseStream<T, Stream<T>> {
	  /**
	   * Value holder for various stream operations.
	   */
	  static final class ValueConsumer<T> implements Consumer<T> {
	    T value;

	    @Override
	    public void accept(T value) {
	      this.value = value;
	    }
	  }

	  @JsIgnore
	  static <T> Stream.Builder<T> builder() {
		  return null;
	  }

	  static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
		  return null;

	  }

	  static <T> Stream<T> empty() {
		  return null;
	  }

	  static <T> Stream<T> generate(Supplier<T> s) {
		  return null;
	  }

	  static <T> Stream<T> iterate(T seed, UnaryOperator<T> f) {
		  return null;
	  }

	  static <T> Stream<T> of(T t) {
		  return null;
	  }

	  @JsMethod(name = "ofArray")
	  static <T> Stream<T> of(T... values) {
		  return null;
	  }

	  /**
	   * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.Builder.html">
	   * the official Java API doc</a> for details.
	   */
	  public interface Builder<T> extends Consumer<T> {
	    @Override
	    void accept(T t);

	    default Stream.Builder<T> add(T t) {
	      accept(t);
	      return this;
	    }

	    Stream<T> build();
	  }

	  boolean allMatch(Predicate<? super T> predicate);

	  boolean anyMatch(Predicate<? super T> predicate);

	  <R, A> R collect(Collector<? super T, A, R> collector);

	  @JsMethod(name = "collectWithCombiner")
	  <R> R collect(
	      Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner);

	  long count();

	  Stream<T> distinct();

	  Stream<T> filter(Predicate<? super T> predicate);

	  Optional<T> findAny();

	  Optional<T> findFirst();

	  <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

	  @JsIgnore
	  DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

	  @JsIgnore
	  IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

	  @JsIgnore
	  LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

	  void forEach(Consumer<? super T> action);

	  void forEachOrdered(Consumer<? super T> action);

	  Stream<T> limit(long maxSize);

	  <R> Stream<R> map(Function<? super T, ? extends R> mapper);

	  @JsIgnore
	  DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

	  @JsIgnore
	  IntStream mapToInt(ToIntFunction<? super T> mapper);

	  @JsIgnore
	  LongStream mapToLong(ToLongFunction<? super T> mapper);

	  Optional<T> max(java.util.Comparator<? super T> comparator);

	  Optional<T> min(java.util.Comparator<? super T> comparator);

	  boolean noneMatch(Predicate<? super T> predicate);

	  Stream<T> peek(Consumer<? super T> action);

	  Optional<T> reduce(BinaryOperator<T> accumulator);

	  @JsMethod(name = "reduceWithIdentity")
	  T reduce(T identity, BinaryOperator<T> accumulator);

	  @JsMethod(name = "reduceWithIdentityAndCombiner")
	  <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

	  Stream<T> skip(long n);

	  Stream<T> sorted();

	  @JsMethod(name = "sortedWithComparator")
	  Stream<T> sorted(java.util.Comparator<? super T> comparator);

	  Object[] toArray();

	  @JsIgnore
	  <A> A[] toArray(IntFunction<A[]> generator);

	  /**
	   * Object to Object map spliterator.
	   * @param <U> the input type
	   * @param <T> the output type
	   */
	  static final class MapToObjSpliterator<U, T> extends java.util.Spliterators.AbstractSpliterator<T> {
	    private final Function<? super U, ? extends T> map;
	    private final java.util.Spliterator<U> original;

	    public MapToObjSpliterator(Function<? super U, ? extends T> map, java.util.Spliterator<U> original) {
	      super(
	          original.estimateSize(),
	          original.characteristics() & ~(java.util.Spliterator.SORTED | java.util.Spliterator.DISTINCT));
	      
	      this.map = map;
	      this.original = original;
	    }

	    @Override
	    public boolean tryAdvance(final Consumer<? super T> action) {
	      return original.tryAdvance(u -> action.accept(map.apply(u)));
	    }
	  }

	  /**
	   * Object to Int map spliterator.
	   * @param <T> the input type
	   */
	  static final class MapToIntSpliterator<T> extends java.util.Spliterators.AbstractIntSpliterator {
	    private final ToIntFunction<? super T> map;
	    private final java.util.Spliterator<T> original;

	    public MapToIntSpliterator(ToIntFunction<? super T> map, java.util.Spliterator<T> original) {
	      super(
	          original.estimateSize(),
	          original.characteristics() & ~(java.util.Spliterator.SORTED | java.util.Spliterator.DISTINCT));
	      
	      this.map = map;
	      this.original = original;
	    }

	    @Override
	    public boolean tryAdvance(final IntConsumer action) {
	      return original.tryAdvance(u -> action.accept(map.applyAsInt(u)));
	    }
	  }

	  /**
	   * Object to Long map spliterator.
	   * @param <T> the input type
	   */
	  static final class MapToLongSpliterator<T> extends java.util.Spliterators.AbstractLongSpliterator {
	    private final ToLongFunction<? super T> map;
	    private final java.util.Spliterator<T> original;

	    public MapToLongSpliterator(ToLongFunction<? super T> map, java.util.Spliterator<T> original) {
	      super(
	          original.estimateSize(),
	          original.characteristics() & ~(java.util.Spliterator.SORTED | java.util.Spliterator.DISTINCT));
	      
	      this.map = map;
	      this.original = original;
	    }

	    @Override
	    public boolean tryAdvance(final LongConsumer action) {
	      return original.tryAdvance(u -> action.accept(map.applyAsLong(u)));
	    }
	  }

	  /**
	   * Object to Double map spliterator.
	   * @param <T> the input type
	   */
	  static final class MapToDoubleSpliterator<T> extends java.util.Spliterators.AbstractDoubleSpliterator {
	    private final ToDoubleFunction<? super T> map;
	    private final java.util.Spliterator<T> original;

	    public MapToDoubleSpliterator(ToDoubleFunction<? super T> map, java.util.Spliterator<T> original) {
	      super(
	          original.estimateSize(),
	          original.characteristics() & ~(java.util.Spliterator.SORTED | java.util.Spliterator.DISTINCT));
	      
	      this.map = map;
	      this.original = original;
	    }

	    @Override
	    public boolean tryAdvance(final DoubleConsumer action) {
	      return original.tryAdvance(u -> action.accept(map.applyAsDouble(u)));
	    }
	  }

	  /**
	   * Object filter spliterator.
	   * @param <T> the type of data to iterate over
	   */
	  static final class FilterSpliterator<T> extends java.util.Spliterators.AbstractSpliterator<T> {
	    private final Predicate<? super T> filter;
	    private final java.util.Spliterator<T> original;

	    private boolean found;

	    public FilterSpliterator(Predicate<? super T> filter, java.util.Spliterator<T> original) {
	      super(original.estimateSize(), original.characteristics() & ~Spliterator.SIZED);
	      
	      this.filter = filter;
	      this.original = original;
	    }

	    @Override
	    public java.util.Comparator<? super T> getComparator() {
	      return original.getComparator();
	    }

	    @Override
	    public boolean tryAdvance(final Consumer<? super T> action) {
	      found = false;
	      while (!found
	          && original.tryAdvance(
	              item -> {
	                if (filter.test(item)) {
	                  found = true;
	                  action.accept(item);
	                }
	              })) {
	        // do nothing, work is done in tryAdvance
	      }

	      return found;
	    }
	  }

	  /**
	   * Object skip spliterator.
	   * @param <T> the type of data to iterate over
	   */
	  static final class SkipSpliterator<T> extends java.util.Spliterators.AbstractSpliterator<T> {
	    private long skip;
	    private final java.util.Spliterator<T> original;

	    public SkipSpliterator(long skip, java.util.Spliterator<T> original) {
	      super(
	          original.hasCharacteristics(java.util.Spliterator.SIZED)
	              ? Math.max(0, original.estimateSize() - skip)
	              : Long.MAX_VALUE,
	          original.characteristics());
	      this.skip = skip;
	      this.original = original;
	    }

	    @Override
	    public java.util.Comparator<? super T> getComparator() {
	      return original.getComparator();
	    }

	    @Override
	    public boolean tryAdvance(Consumer<? super T> action) {
	      while (skip > 0) {
	        if (!original.tryAdvance(ignore -> { })) {
	          return false;
	        }
	        skip--;
	      }
	      return original.tryAdvance(action);
	    }
	  }

	  /**
	   * Object limit spliterator.
	   * @param <T> the type of data to iterate over
	   */
	  static final class LimitSpliterator<T> extends java.util.Spliterators.AbstractSpliterator<T> {
	    private final long limit;
	    private final java.util.Spliterator<T> original;
	    private int position = 0;

	    public LimitSpliterator(long limit, java.util.Spliterator<T> original) {
	      super(
	          original.hasCharacteristics(java.util.Spliterator.SIZED)
	              ? Math.min(original.estimateSize(), limit)
	              : Long.MAX_VALUE,
	          original.characteristics());
	      this.limit = limit;
	      this.original = original;
	    }

	    @Override
	    public java.util.Comparator<? super T> getComparator() {
	      return original.getComparator();
	    }

	    @Override
	    public boolean tryAdvance(Consumer<? super T> action) {
	      if (position >= limit) {
	        return false;
	      }
	      boolean result = original.tryAdvance(action);
	      position++;
	      return result;
	    }
	  }

		// ################################################
		// ## . . . . . . . TFJS Additions . . . . . . . ##
		// ################################################

		/** Returns a native java iterable whose implementation is based on the iterator. */
		JsIterable<T> iterable();

		Stream<T> filterJs(Lambdas.JsPredicate<? super T> predicate);

		<R> Stream<R> mapJs(Lambdas.JsUnaryFunction<? super T, ? extends R> mapper);

		<R> Stream<R> flatMapJs(Lambdas.JsUnaryFunction<? super T, ? extends Stream<? extends R>> mapper);

		void forEachJs(Lambdas.JsConsumer<? super T> action);

		void forEachOrderedJs(Consumer<? super T> action);

}
