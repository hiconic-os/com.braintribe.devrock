package com.braintribe.build.artifact.test.repolet;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.braintribe.utils.IOTools;

public class StreambasedTemplateProvider implements Supplier<String> {
	String content;

	public StreambasedTemplateProvider( InputStream stream) {	
		try {
			content = IOTools.slurp(stream, "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException("cannot read template", e);
		}
	}
	@Override
	public String get() {		
		return content;
	}

}
