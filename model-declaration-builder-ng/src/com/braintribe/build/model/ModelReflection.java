package com.braintribe.build.model;

import com.braintribe.build.model.entity.Entity;

public interface ModelReflection {	
	Entity load(String className);

}