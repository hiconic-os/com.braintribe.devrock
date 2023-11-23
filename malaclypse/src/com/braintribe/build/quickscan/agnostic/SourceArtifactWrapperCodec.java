// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.agnostic;

import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.panther.SourceArtifact;

public class SourceArtifactWrapperCodec extends HashSupportWrapperCodec< SourceArtifact> {
	private SourceArtifactWrapperCodecMode mode = SourceArtifactWrapperCodecMode.artifact;
	
	public SourceArtifactWrapperCodec(SourceArtifactWrapperCodecMode mode) {
		super(true);
		this.mode = mode;
	}

	@Override
	protected int entityHashCode(SourceArtifact e) {
		switch (mode) {
			case name:
				return (e.getArtifactId()).hashCode();		
			case identification:
				return (e.getGroupId() + ":" + e.getArtifactId()).hashCode();
			case artifact:
				return (e.getGroupId() + ":" + e.getArtifactId() + "#" + e.getVersion()).hashCode();
			default:
			case complexIdentification:
				return (e.getGroupId() + ":" + e.getArtifactId() + "|" + e.getRepository().getName()).hashCode();					
		}
		
	}

	@Override
	protected boolean entityEquals(SourceArtifact e1, SourceArtifact e2) {
		
		switch (mode) {
			case artifact:
				if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
					return false;
				if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
					return false;		
				if (!e1.getVersion().equalsIgnoreCase(e2.getVersion()))
					return false;
				break;
			case identification:
				if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
					return false;
				if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
					return false;
				break;
			case name:
				if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
					return false;
				break;			
			default:
			case complexIdentification:
				if (!e1.getArtifactId().equalsIgnoreCase(e2.getArtifactId()))
					return false;
				if (!e1.getGroupId().equalsIgnoreCase(e2.getGroupId()))
					return false;					
				if (!e1.getRepository().getName().equalsIgnoreCase(e2.getRepository().getName()))
					return false;
				break;		
		}
		
		return true;
	}

	
}
