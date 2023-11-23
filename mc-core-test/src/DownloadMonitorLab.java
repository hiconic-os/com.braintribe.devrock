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
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventEmitter;
import com.braintribe.devrock.mc.core.commons.DownloadMonitor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class DownloadMonitorLab {
	public static void main(String[] args) {
		
		
		DownloadMonitor monitor = new DownloadMonitor(new EventEmitter() {
			
			@Override
			public <E extends GenericEntity> void removeListener(EntityType<E> eventType,
					EntityEventListener<? super E> listener) {
			}
			
			@Override
			public <E extends GenericEntity> void addListener(EntityType<E> eventType,
					EntityEventListener<? super E> listener) {
			}
		});
		
		ConsoleConfiguration.install(new PlainSysoutConsole());
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
