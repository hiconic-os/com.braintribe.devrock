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
