package com.braintribe.build.artifacts.mc.wire.classwalk.external.contract;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.MagicScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.ScopeTreatement;

/**
 * lill' helper class to generate the standard magic scopes, i.e. compile and runtime
 *
 * for documentation about the magic scopes, see here : https://maven.apache.org/ant-tasks/examples/dependencies.html<br/>
 * note: system scope is there, but isn't supported by MC yet (anyhow, it's kind *not* tbe maven style)
 * @author pit
 *
 */
public class Scopes {

	/**
	 * generate a magic scope for a compile walk
	 * @return - a {@link Set} of {@link Scope} for a compile walk
	 */
	public static Set<Scope> compileScopes() {
		MagicScope classpathScope = MagicScope.T.create();
		classpathScope.setName("compileScope");
		
		DependencyScope compileDepScope = DependencyScope.T.create();
		compileDepScope.setName("compile");
		compileDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		classpathScope.getScopes().add( compileDepScope);
		
		DependencyScope providedDepScope = DependencyScope.T.create();
		providedDepScope.setName("provided");
		providedDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);		
		classpathScope.getScopes().add( providedDepScope);

		//  
		DependencyScope systemDepScope = DependencyScope.T.create();
		systemDepScope.setName("system");
		systemDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		classpathScope.getScopes().add( systemDepScope);
		
		
		DependencyScope testScope = DependencyScope.T.create();
		testScope.setName("test");
		testScope.setScopeTreatement( ScopeTreatement.EXCLUDE);		
		classpathScope.getScopes().add( testScope);
				
		return Collections.singleton( classpathScope);
	}
	
	/**
	 * generate a magic scope for a runtime walk 
	 * @return - a {@link Set} of {@link Scope} for a runtime walk 
	 */
	public static Set<Scope> runtimeScopes() {
		MagicScope runtimeScope = MagicScope.T.create();
		runtimeScope.setName("runtimeScope");
		
		DependencyScope compileDepScope = DependencyScope.T.create();
		compileDepScope.setName("compile");
		compileDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		runtimeScope.getScopes().add( compileDepScope);
		
		DependencyScope providedDepScope = DependencyScope.T.create();
		providedDepScope.setName("runtime");
		providedDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);		
		runtimeScope.getScopes().add( providedDepScope);
		
		return Collections.singleton( runtimeScope);
	}
	
	/**
	 * generate a magic scope for a runtime walk 
	 * @return - a {@link Set} of {@link Scope} for a runtime walk 
	 */
	public static Set<Scope> testScopes() {
		MagicScope testScope = MagicScope.T.create();
		testScope.setName("testScope");
		
		DependencyScope compileDepScope = DependencyScope.T.create();
		compileDepScope.setName("compile");
		compileDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		testScope.getScopes().add( compileDepScope);
		
		DependencyScope providedDepScope = DependencyScope.T.create();
		providedDepScope.setName("provided");
		providedDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);		
		testScope.getScopes().add( providedDepScope);
		
		DependencyScope runtimeDepScope = DependencyScope.T.create();
		runtimeDepScope.setName("runtime");
		runtimeDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		testScope.getScopes().add( runtimeDepScope);

		DependencyScope testDepScope = DependencyScope.T.create();
		testDepScope.setName("test");
		testDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		testScope.getScopes().add( testDepScope);

		DependencyScope systemDepScope = DependencyScope.T.create();
		systemDepScope.setName("system");
		systemDepScope.setScopeTreatement( ScopeTreatement.INCLUDE);
		testScope.getScopes().add( systemDepScope);
		
		return Collections.singleton( testScope);
	}


	public static Set<Scope> buildScopes(List<String> scopes) {
		MagicScope magicScope = MagicScope.T.create();
		magicScope.setName("custom");

		for (String scope : scopes) {
			DependencyScope compileScope = DependencyScope.T.create();
			compileScope.setName( scope);
			compileScope.setScopeTreatement( ScopeTreatement.INCLUDE);
			magicScope.getScopes().add( compileScope);
		}
		return Collections.singleton(magicScope);
	}
}
