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

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.github.benmanes.caffeine.cache.Expiry;

/**
 * implementation for Caffeine's stale system - standard impl 
 * 
 * @author pit / dirk
 *
 * @param <K>
 * @param <V>
 */
public class PassiveCacheExpiry<K,V> implements Expiry<K, V> {

	@Override
	public long expireAfterCreate(@NonNull K key, @NonNull V value, long currentTime) {
		return Long.MAX_VALUE;
	}

	@Override
	public long expireAfterUpdate(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
		return currentDuration;
	}

	@Override
	public long expireAfterRead(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
		return currentDuration;
	}

}
