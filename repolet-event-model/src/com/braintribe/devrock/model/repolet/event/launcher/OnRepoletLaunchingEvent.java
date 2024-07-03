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
package com.braintribe.devrock.model.repolet.event.launcher;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * send by the launcher before it analyzes the configuration at launch time
 * @author pit
 *
 */
//TODO : turn repolet configuration instances to be able to send the configuration used
public interface OnRepoletLaunchingEvent extends RepoletLauncherEvent {
	
	EntityType<OnRepoletLaunchingEvent> T = EntityTypes.T(OnRepoletLaunchingEvent.class);
	String assignedPort = "assignedPort";
	
	
	int getAssignedPort();
	void setAssignedPort(int value);


}
