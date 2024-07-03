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
package com.braintribe.artifacts.test.maven.pom.marshall.validator;

import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Dependency;

public class BasicValidatorContext implements ValidatorContext {
	private Dependency dependency;
	
	private String groupId;
	private String artifactId;
	private String version;
	
	private String scope;
	private String type;
	private Boolean optional;
	
	private String group;
	private List<String> tags;
	private Map<String,String> redirections;
	private Map<String,String> virtualParts;
	
	public BasicValidatorContext() {
	}
	
	public BasicValidatorContext( String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;		
	}
	
	public BasicValidatorContext( Dependency dependency, String groupId, String artifactId, String version) {
		this.dependency = dependency;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;		
	}
	
	public BasicValidatorContext( Dependency dependency, String groupId, String artifactId, String version, List<String> tags) {
		this.dependency = dependency;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.tags = tags;		
	}
	
	
	

	@Override
	public Dependency dependency() {
		return dependency;
	}
	@Configurable @Required
	public void setDependency(Dependency dependency) {
		this.dependency = dependency;
	}

	@Override
	public String groupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public String artifactId() {	
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
	@Override
	public String version() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String scope() {	
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String type() {	
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	@Override
	public Boolean optional() {
		return optional;
	}
	public void setOptional(Boolean optional) {
		this.optional = optional;
	}
	@Override
	public String group() {	
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public List<String> tags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@Override
	public Map<String, String> redirects() {	
		return redirections;
	}
	public void setRedirects(Map<String, String> redirections) {
		this.redirections = redirections;
	}
	@Override
	public Map<String, String> virtualParts() {	
		return virtualParts;
	}
	public void setVirtualParts(Map<String, String> virtualParts) {
		this.virtualParts = virtualParts;
	}
	

}
