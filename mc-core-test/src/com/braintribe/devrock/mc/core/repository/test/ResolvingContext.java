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
package com.braintribe.devrock.mc.core.repository.test;

import java.util.List;

import com.braintribe.devrock.mc.api.resolver.ChecksumPolicy;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * cheap lill' context to ease parameter transfer within the test
 * @author pit
 *
 */
public class ResolvingContext {
	
	private String root;
	private CompiledArtifactIdentification ci;
	private PartIdentification part;
	private List<PartIdentification> parts;
	private ChecksumPolicy cPolicy;
	private String userName;
	private String password;
	private boolean ignoreHashHeader;
	
	public ResolvingContext(String root, CompiledArtifactIdentification ci, PartIdentification pi, ChecksumPolicy cp) {
		this.root = root;
		this.ci = ci;
		this.part = pi;
		this.cPolicy = cp; 
	}
	public ResolvingContext(String root, CompiledArtifactIdentification ci, PartIdentification pi, ChecksumPolicy cp, String userName, String password) {
		this.root = root;
		this.ci = ci;
		this.part = pi;
		this.cPolicy = cp;
		this.userName = userName;
		this.password = password;
	}
	
	
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
	}
	public CompiledArtifactIdentification getCi() {
		return ci;
	}
	public void setCi(CompiledArtifactIdentification ci) {
		this.ci = ci;
	}
	public PartIdentification getPart() {
		return part;
	}
	public void setPart(PartIdentification part) {
		this.part = part;
	}
	public ChecksumPolicy getChecksumPolicy() {
		return cPolicy;
	}
	public void setChecksumPolicy(ChecksumPolicy cPolicy) {
		this.cPolicy = cPolicy;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public ChecksumPolicy getcPolicy() {
		return cPolicy;
	}
	public void setcPolicy(ChecksumPolicy cPolicy) {
		this.cPolicy = cPolicy;
	}
	public boolean isIgnoreHashHeader() {
		return ignoreHashHeader;
	}
	public void setIgnoreHashHeader(boolean ignoreHashHeader) {
		this.ignoreHashHeader = ignoreHashHeader;
	}
	public List<PartIdentification> getParts() {
		return parts;
	}
	public void setParts(List<PartIdentification> parts) {
		this.parts = parts;
	}
	
	
	
	
}
