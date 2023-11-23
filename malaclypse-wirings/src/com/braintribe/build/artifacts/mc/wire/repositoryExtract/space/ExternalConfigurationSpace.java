package com.braintribe.build.artifacts.mc.wire.repositoryExtract.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.ExternalConfigurationContract;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

/**
 * a space to bind the configuration as specified via the module
 * @author pit
 *
 */
public class ExternalConfigurationSpace implements ExternalConfigurationContract {
	
	private List<Pattern> globalExclusions;
	private List<String> terminals;
	private String settingsPath;
	private String repositoryPath;
	
	public void setGlobalExclusions(Collection<String> exclusions) {
		if (exclusions != null) {
			globalExclusions = new ArrayList<>();
			for (String exclusion : exclusions) {
				globalExclusions.add( Pattern.compile(exclusion));
			}			
		}
	}
	
	@Override
	public List<Pattern> globalExclusions() {	
		return this.globalExclusions;
	}

	@Configurable @Required
	public void setTerminals(List<String> terminals) {
		this.terminals = terminals;
	}
	
	@Configurable @Required
	public void setTerminal(String terminal) {
		this.terminals = new ArrayList<>();
		this.terminals.add( terminal);
	}
	
	@Override
	public List<String> terminals() {	
		return this.terminals;
	}
	@Configurable
	public void setSettingsOverridePath(String settingsPath) {
		this.settingsPath = settingsPath;
	}
	@Override
	public String settingsOverride() {
		return settingsPath;
	}
	
	@Configurable
	public void setRepositoryOverridePath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}
	@Override
	public String repositoryOverride() {
		return repositoryPath;
	}
	
	
	

}
