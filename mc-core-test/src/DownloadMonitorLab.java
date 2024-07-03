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
