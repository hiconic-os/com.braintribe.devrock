package com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract;

import java.util.List;
import java.util.regex.Pattern;

import com.braintribe.wire.api.space.WireSpace;

/**
 * the configuration contract 
 * 
 * @author pit
 *
 */
public interface ExternalConfigurationContract extends WireSpace {
	/**
	 * @return - a {@link List} of {@link Pattern} for qualified (condensed) artifact names
	 */
	List<Pattern> globalExclusions();
	/**
	 * @return - a {@link List} of qualified (condensed) artifact names to act as terminals (entry points)
	 */
	List<String> terminals();
	
	/**
	 * @return - the location of the settings.xml to use, null if standard search should happen
	 */
	String settingsOverride();
	/**
	 * @return - the location of the local repository structure to use, null if standard repo (as from settings.xml) should be taken 
	 */
	String repositoryOverride();
	
}
