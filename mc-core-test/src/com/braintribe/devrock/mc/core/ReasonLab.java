// ============================================================================
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
package com.braintribe.devrock.mc.core;

import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.devrock.mc.core.commons.McReasonOutput;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependency;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.Version;

public class ReasonLab {
	public static void main(String[] args) {
		ConsoleConfiguration.install(new PlainSysoutConsole());
		ConsoleConfiguration.install(new PrintStreamConsole(System.out));
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create("foo", "bar", "1.0");
		CompiledDependency cd = CompiledDependency.create("foo", "bar", Version.parse("1.0"), "compile", "classes", "jar");
		
		
		UnresolvedDependency reason = TemplateReasons.build(UnresolvedDependency.T)
				.enrich(r -> r.setDependency(cdi)) //
				.cause(TemplateReasons.build(UnresolvedDependencyVersion.T).enrich(r -> r.setVersion(cd.getVersion())).toReason()) //
				.toReason();
		
		McReasonOutput reasonOutput = new McReasonOutput();
		reasonOutput.println(reason);
	}
	
	
}
