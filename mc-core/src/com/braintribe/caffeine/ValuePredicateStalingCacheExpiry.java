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
package com.braintribe.caffeine;

import java.util.function.Predicate;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * impl for cache expiry based on a predicate
 * @author pit / dirk
 *
 * @param <K>
 * @param <V>
 */
public class ValuePredicateStalingCacheExpiry<K,V> extends PassiveCacheExpiry<K, V> {
	private Predicate<? super V> predicate;

	public ValuePredicateStalingCacheExpiry(Predicate<? super V> predicate) {
		super();
		this.predicate = predicate;
	}
	
	@Override
	public long expireAfterRead(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
		if (predicate.test( value)) {
			return 0;
		}
		return currentDuration;
	}
	
}
