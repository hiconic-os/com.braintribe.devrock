package com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.artifact.Identification;

public class ArtifactBias {

	private Identification identification;
	private List<String> activeRepositories = new ArrayList<>();
	private List<String> inactiveRepositories = new ArrayList<>();
	
	public ArtifactBias() {
		identification = Identification.T.create();
	}
	
	public ArtifactBias( String string) {
		String line = string.trim();
		int colonP = line.indexOf(':');
		int colonSc = line.indexOf(';');

		
		identification = Identification.T.create();
		if (colonP < 0) {
			if (colonSc < 0) {
				identification.setGroupId( line);						
			}
			else {
				identification.setGroupId( line.substring(0, colonSc));
			}
			identification.setArtifactId( ".*");
		}
		else {
			String grp = line.substring(0, colonP);
			if (colonSc < 0) { 
				String arfct = line.substring( colonP+1);					
				identification.setArtifactId( arfct);
			}
			else {
				String arfct = line.substring( colonP+1, colonSc);					
				identification.setArtifactId( arfct);
			}
			identification.setGroupId( grp);
		}
		 
		if (colonSc > 0) {
			String remainder = line.substring(colonSc+1);
			String [] repoIds = remainder.split(",");
			for (String repoId : repoIds) {
				if (repoId.startsWith( "!")) {
					inactiveRepositories.add( repoId.substring(1));
				}
				else {
					activeRepositories.add(repoId);
				}
			}
		}
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( identification.getGroupId());
		if (identification.getArtifactId() != null && !identification.getArtifactId().equalsIgnoreCase( ".*")) {
			builder.append(':');
			builder.append( identification.getArtifactId());
		}
		if (activeRepositories.size() > 0 || inactiveRepositories.size() > 0) {
			builder.append(';');
			for (String activeRepoId : activeRepositories) {
				if (builder.charAt( builder.length() - 1) != ';')
					builder.append( ',');
				builder.append( activeRepoId);				
			}
			for (String inactiveRepoId : inactiveRepositories) {
				if (builder.charAt( builder.length() - 1) != ';')
					builder.append( ',');
				builder.append( "!" + inactiveRepoId);				
			}
		}		
		return builder.toString();
			
	}
	
	public boolean matches( Identification identification) {
		if (this.identification.getGroupId() != null) {
			if (!identification.getGroupId().matches( this.identification.getGroupId()))
				return false;
		}
		if (this.identification.getArtifactId() != null) {
			if (!identification.getArtifactId().matches( this.identification.getArtifactId())) 
				return false;
		}
		return true;
	}
	
	public Identification getIdentification() {
		return identification;
	}
	public void setIdentification(Identification identification) {
		this.identification = identification;
	}
	public List<String> getActiveRepositories() {
		return activeRepositories;
	}
	public void setActiveRepositories(List<String> activeRepositories) {
		this.activeRepositories = activeRepositories;
	}
	public List<String> getInactiveRepositories() {
		return inactiveRepositories;
	}
	public void setInactiveRepositories(List<String> inactiveRepositories) {
		this.inactiveRepositories = inactiveRepositories;
	}
	
	public boolean hasLocalBias() {
		
		if (activeRepositories.isEmpty() && inactiveRepositories.isEmpty()) {
			return true;
		}
		else {
			if (activeRepositories.contains( "local") && !inactiveRepositories.contains("local"))
				return true;
		}
		return false;
	}
	
	public boolean hasPositiveBias() {
		return !activeRepositories.isEmpty();// && inactiveRepositories.isEmpty();
	}
	
	public boolean hasPositiveBias(String repositoryId) {
		return activeRepositories.contains(repositoryId);
	}
	
	public boolean hasNegativeBias() {
		return !inactiveRepositories.isEmpty();// && activeRepositories.isEmpty();
	}
	
	public boolean hasNegativeBias( String repositoryId) {
		return inactiveRepositories.contains(repositoryId);
	}
	
}
