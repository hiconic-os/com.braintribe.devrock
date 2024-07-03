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
package com.braintribe.devrock.mc.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * EventHub acts as {@link EventEmitter} to serve event producers
 * and as {@link EventBroadcaster} to serve {@link EntityEventListener}.
 * 
 * @author Dirk Scheffler
 *
 */
public class EventHub implements EventEmitter, EventBroadcaster {
	private Map<EntityType<?>, List<EntityEventListener<?>>> listeners = new ConcurrentHashMap<>();
	
	@Override
	public <E extends GenericEntity> void addListener(EntityType<E> eventType,
			EntityEventListener<? super E> listener) {
		listeners.compute(eventType, (t, l) -> {
			if (l == null)
				l = new ArrayList<>();
			
			l.add(listener);
			
			return l;
		});
		
	}
	
	@Override
	public <E extends GenericEntity> void removeListener(EntityType<E> eventType,
			EntityEventListener<? super E> listener) {
		listeners.computeIfPresent(eventType, (t, l) -> {
			l.remove(listener);
			return l.isEmpty()? null: l;
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void sendEvent(GenericEntity event) {
		List<EntityEventListener<GenericEntity>> effectiveListeners = new ArrayList<>();
		
		listeners.compute(event.entityType(), (t, l) -> {
			if (l != null)
				effectiveListeners.addAll( (List<EntityEventListener<GenericEntity>>) (List<?>) l);
			return l;
		});
	
		effectiveListeners.stream().forEach( l -> l.onEvent(null, event));
	}
	
	
}
