package com.braintribe.build.artifact.walk.multi.clash;

import java.util.function.Supplier;

import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;

public interface ConfigurableClashResolver extends ClashResolver {
	/**
	 * sets the provider for the terminal artifact - the root of the tree 
	 * @param provider - the {@link Supplier} to provide the terminal {@link Artifact}
	 */
	@Deprecated
	void setTerminalArtifactProvider( Supplier<Artifact> provider);	
	/**
	 * sets the dependency merger instance 
	 * @param merger - the {@link DependencyMerger} 
	 */
	void setDependencyMerger( DependencyMerger merger);
	
	/**
	 * sets the sorter that defines the sequence of the clashes that resolved 
	 * @param sorter - the {@link InitialDependencyPrecedenceSorter} to use 
	 */
	void setInitialPrecedenceSorter( InitialDependencyPrecedenceSorter sorter);
	
	/**
	 * sets the leniency of the clash resolver.. if set to false, clashes will lead 
	 * to exceptions, and with {@link #setLenicenyFocus(ClashResolverLeniencyFocus)} you can
	 * control, on which level it will abort.
	 * @param leniency
	 */
	void setLeniency( boolean leniency);
	
	
	/**
	 * @param resolvingInstant
	 */
	void setResolvingInstant( ResolvingInstant resolvingInstant);
}
