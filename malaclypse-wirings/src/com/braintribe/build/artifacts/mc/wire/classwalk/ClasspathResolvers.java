package com.braintribe.build.artifacts.mc.wire.classwalk;

import java.util.function.Consumer;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.CompileClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.RuntimeClasspathResolverExternalSpace;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;

/**
 * class path resolvers do a standard traversion of the dependency tree, starting at the passed terminal, they resolve all clashes (if any),
* and enrich any solution (i.e. download the required parts), all while using the logic as defined by the contracts and contexts,
* <br/>
*  besides two preconfigured {@link WireContext} (one for compile- the other for the runtime-scope), one configurable {@link WireContext} is sported<br/>
*  during the {@link WireContext}'s lifespan, all caches remain active, plus the wiring stays (if configured)  
*  
 * @author pit
 *
 */
public interface ClasspathResolvers {

	/**
	 * configurable {@link ClasspathResolverContract}
	 * 
	 * @param configurer - adds a custom external configuration 
	 * @return - a {@link WireContext} with the {@link ClasspathResolverContract}
	 */
	public static WireContext<ClasspathResolverContract> classpathResolverContext(Consumer<WireContextBuilder<ClasspathResolverContract>> configurer) {		
		WireContextBuilder<ClasspathResolverContract> builder = Wire.context(ClasspathResolverContract.class)
				.bindContracts(ClasspathResolvers.class.getPackage().getName());	
		configurer.accept(builder);
		
		return builder.build();
	}
	
	
	/**
	 * a {@link ClasspathResolverContract} set up for a standard compile walk 
	 * @return - a {@link WireContext} with a {@link ClasspathResolverContract}, parametrized by the {@link ClasspathResolverExternalContract} for compile
	 */
	public static WireContext<ClasspathResolverContract> compileContext() {
		ClasspathResolverExternalContract cfgSpace = new CompileClasspathResolverExternalSpace();
		
		WireContext<ClasspathResolverContract> contex = classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfgSpace);	
		});
	
		return contex;
	}
	/**
	 * a {@link ClasspathResolverContract} set up for a standard runtime walk 
	 * @return - a {@link WireContext} with a {@link ClasspathResolverContract}, parametrized by the {@link ClasspathResolverExternalContract} for runtime
	 */
	public static WireContext<ClasspathResolverContract> launchContext() {
		ClasspathResolverExternalContract cfgSpace = new RuntimeClasspathResolverExternalSpace();
		
		WireContext<ClasspathResolverContract> contex = classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfgSpace);	
		});
	
		return contex;
	}
}
