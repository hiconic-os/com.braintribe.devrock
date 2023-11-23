package com.braintribe.build.artifacts.mc.wire.repositoryExtract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.braintribe.build.artifacts.mc.wire.buildwalk.BuildDependencyResolverWireModule;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.FilterConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.ExternalConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.RepositoryExtractContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.space.ExternalConfigurationSpace;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.space.FilterConfigurationSpace;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * a wire module for the RepositoryExtract - extracts all referenced artifacts from a passed dependency as starting point.
 * 
 * @author pit
 *
 */
public class RepositoryExtractWireModule implements WireTerminalModule<RepositoryExtractContract> {
	private ExternalConfigurationSpace externalConfiguration = new ExternalConfigurationSpace();
	
	/**
	 * an instance to handle a single terminal with standard setup
	 * @param terminal - the single qualified name of terminal dependency to process (will be autoranged)
	 * @param exclusions - the qualified names of the artifacts you want to skip, supports regular expressions 
	 */
	public RepositoryExtractWireModule(String terminal, String ... exclusions) {
		if (exclusions != null) {
			externalConfiguration.setGlobalExclusions( Arrays.asList( exclusions));
		}
		externalConfiguration.setTerminal(terminal);
	
	}
	
	/**
	 * an instance to handle a single terminal with a custom setup (settings.xml, local repo)
	 * @param overridePathToSettings - a fully qualified path to an *alternative* settings.xml, null if standard should be used
	 * @param overridePathToLocalRepo - a fully qualified path to an *alternative* local repository, null if standard (as derived from settings.xml) should be used
	 * @param terminal - the single qualified name of terminal dependency to process (will be autoranged)
	 * @param exclusions - the qualified names of the artifacts you want to skip, supports regular expressions
	 */
	public RepositoryExtractWireModule( String overridePathToSettings, String overridePathToLocalRepo, String terminal, String ...exclusions) {
		if (exclusions != null) {
			externalConfiguration.setGlobalExclusions( Arrays.asList( exclusions));
		}
		externalConfiguration.setSettingsOverridePath(overridePathToSettings);
		externalConfiguration.setRepositoryOverridePath(overridePathToLocalRepo);
		externalConfiguration.setTerminal(terminal);
	}
	
	/**
	 * an instance to handle multiple terminals with the standard setup
	 * @param terminals - a {@link Collection} of qualified names of the terminal dependencies to process IN TURN, will be autoranged.
	 * @param exclusions - the qualified names of the artifacts you want to skip, supports regular expressions
	 */
	public RepositoryExtractWireModule(Collection<String> terminals, String ... exclusions) {
		if (exclusions != null) {
			externalConfiguration.setGlobalExclusions( Arrays.asList( exclusions));
		}
		externalConfiguration.setTerminals( new ArrayList<>( terminals));
	}
	
	/**
	 * an instance to handle multiple terminals with a custom setup (settings.xml, local repo)
 	 * @param overridePathToSettings - a fully qualified path to an *alternative* settings.xml, null if standard should be used
	 * @param overridePathToLocalRepo - a fully qualified path to an *alternative* local repository, null if standard (as derived from settings.xml) should be used
	 * @param terminals - a {@link Collection} of qualified names of the terminal dependencies to process IN TURN, will be autoranged.
	 * @param exclusions - the qualified names of the artifacts you want to skip, supports regular expressions
	 */
	public RepositoryExtractWireModule(String overridePathToSettings, String overridePathToLocalRepo, Collection<String> terminals, String ... exclusions) {
		if (exclusions != null) {
			externalConfiguration.setGlobalExclusions( Arrays.asList( exclusions));
		}
		externalConfiguration.setSettingsOverridePath(overridePathToSettings);
		externalConfiguration.setRepositoryOverridePath(overridePathToLocalRepo);
		externalConfiguration.setTerminals( new ArrayList<>( terminals));
	}
	

	@Override
	public List<WireModule> dependencies() {	
		return Lists.list( BuildDependencyResolverWireModule.DEFAULT);
	}

	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract( ExternalConfigurationContract.class, externalConfiguration);
		contextBuilder.bindContract( FilterConfigurationContract.class, FilterConfigurationSpace.class);
	}
	
	
}

