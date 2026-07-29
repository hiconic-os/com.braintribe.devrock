package com.braintribe.build.model;

/**
 * @deprecated Use {@link ModelClassFileReflection}. Kept as a binary/source
 *             compatibility facade for integrations which instantiated the old
 *             implementation directly.
 */
@Deprecated
public class ModelAsmReflection extends ModelClassFileReflection {

	public ModelAsmReflection(ClassLoader classLoader) {
		super(classLoader);
	}

	public static ModelReflection scan(ClassLoader classLoader) {
		return new ModelAsmReflection(classLoader);
	}
}
