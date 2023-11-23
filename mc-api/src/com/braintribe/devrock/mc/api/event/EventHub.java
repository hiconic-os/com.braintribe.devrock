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
