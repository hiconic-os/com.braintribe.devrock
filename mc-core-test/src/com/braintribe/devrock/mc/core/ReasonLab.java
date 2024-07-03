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
