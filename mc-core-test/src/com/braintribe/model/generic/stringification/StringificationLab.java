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
package com.braintribe.model.generic.stringification;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.devrock.mc.core.commons.McReasonOutput;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependency;
import com.braintribe.devrock.model.mc.reason.UnresolvedDependencyVersion;
import com.braintribe.devrock.model.mc.reason.UnresolvedPropertyExpression;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;

public class StringificationLab {
	public static void main(String[] args) {
		ConsoleConfiguration.install(new PrintStreamConsole(System.out));
		Map<String, CompiledDependencyIdentification> map = new HashMap<>();
		
		map.put("one", CompiledDependencyIdentification.create("foo", "fix", "1.0"));
		map.put("two", CompiledDependencyIdentification.create("bar", "fox", "1.0"));
		CompiledDependencyIdentification dependency = CompiledDependencyIdentification.create("bloody", "hell", "4711");
		map.put("three", dependency);
		
		UnresolvedDependencyVersion cause = TemplateReasons.build(UnresolvedDependencyVersion.T).enrich(r -> {
			r.setVersion(VersionExpression.parse("[1.0,1.1)"));
			r.getAvailableVersions().add(Version.create(1,2,1));
			r.getAvailableVersions().add(Version.create(1,2,2));
		}).toReason();
		
		UnresolvedPropertyExpression cause2 = TemplateReasons.build(UnresolvedPropertyExpression.T).enrich(r -> r.setExpression("${foobar}-${fixfox}")).toReason();
			
		UnresolvedDependency reason = TemplateReasons.build(UnresolvedDependency.T).enrich(r -> r.setDependency(dependency)).cause(cause).cause(cause2).toReason();
		
		System.out.println("getText(): " + reason.getText());
		System.out.println("stringify(): \n" + reason.stringify());
		System.out.println("toSelectiveInformation(): " + reason.toSelectiveInformation());
		new McReasonOutput().println(reason);
		
		System.out.println(map);
	}
}
