package com.braintribe.build.artifact.walk.build;

import java.util.function.Predicate;

import com.braintribe.model.artifact.Dependency;

public class ParallelResolvingContext {
	public static final ParallelResolvingContext empty = new ParallelResolvingContext(null, null);
	
	private ParallelResolvingContext parent;
	private Predicate<? super Dependency> acculumativePredicate;
	
	public ParallelResolvingContext(ParallelResolvingContext parent, Predicate<? super Dependency> acculumativePredicate) {
		super();
		this.parent = parent;
		this.acculumativePredicate = acculumativePredicate;
	}

	public boolean isRelevant(Dependency dependency) {
		if (acculumativePredicate != null && !acculumativePredicate.test(dependency))
			return false;
		
		if (parent == null)
			return true;
		
		return parent.isRelevant(dependency);
	}
}
