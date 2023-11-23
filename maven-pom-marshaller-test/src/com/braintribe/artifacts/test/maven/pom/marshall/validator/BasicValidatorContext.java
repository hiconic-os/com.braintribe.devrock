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
