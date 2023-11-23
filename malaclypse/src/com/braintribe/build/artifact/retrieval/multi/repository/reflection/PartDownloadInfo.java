// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection;

import java.io.File;

import com.braintribe.model.artifact.Part;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

/**
 * @author pit
 *
 */
public class PartDownloadInfo {
	private String source;
	private String target;
	private String name;
	private String actualName;
	private Part owningPart;
	private File file;
	
	private RavenhurstBundle bundle;
	private boolean identifiedAsNotTrustworthy;
	private boolean freshDownload;

	public PartDownloadInfo(RavenhurstBundle bundle, String source, String target) {
		this.bundle = bundle;
		this.source = source;
		this.target = target;
	}
	
	public PartDownloadInfo( File file) {
		this.file = file;
	}

	public String getUrl() {
		return bundle.getRepositoryUrl();
	}

	public RavenhurstBundle getBundle() {
		return bundle;
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	public boolean getIdentifiedAsNotTrustworthy() {
		return identifiedAsNotTrustworthy;
	}
	public void setIdentifiedAsNotTrustworthy(boolean identifiedAsNotTrustworthy) {
		this.identifiedAsNotTrustworthy = identifiedAsNotTrustworthy;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

	public String getActualName() {
		return actualName;
	}

	public void setActualName(String actualName) {
		this.actualName = actualName;
	}

	public Part getOwningPart() {
		return owningPart;
	}

	public void setOwningPart(Part owningPart) {
		this.owningPart = owningPart;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFreshDownload(boolean freshDownload) {
		this.freshDownload = freshDownload;
	}
	
	public boolean isFreshDownload() {
		return freshDownload;
	}
}
