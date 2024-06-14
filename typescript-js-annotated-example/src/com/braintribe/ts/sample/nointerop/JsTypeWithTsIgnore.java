package com.braintribe.ts.sample.nointerop;

import jsinterop.annotations.JsType;
import jsinterop.annotations.custom.TsIgnore;

@TsIgnore
@JsType(namespace="should.not.be.found")
public interface JsTypeWithTsIgnore {

	void foobar();
	
}
