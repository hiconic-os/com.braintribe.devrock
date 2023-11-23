// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import javax.tools.SimpleJavaFileObject;

/**
 * A file object used to represent source coming from a string.
 */
public class JavaSource extends SimpleJavaFileObject {
    /**
     * The source code of this "file".
     */
    private URL url;

    /**
     * Constructs a new JavaSourceFromString.
     * @param name the name of the compilation unit represented by this file object
     * @param code the source code for the compilation unit represented by this file object
     */
    public JavaSource(URI uri, URL url) {
        super(uri, Kind.SOURCE);
        this.url = url;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    	try {
        	Reader reader = new InputStreamReader(url.openStream(), "ISO-8859-1");
        	char buffer[] = new char[65536];
        	StringBuilder builder = new StringBuilder();
        	
			try {
				while (true) {
					int i = reader.read(buffer);
					
					if (i == -1) break;

					builder.append(buffer, 0, i);
				}
			}
			finally {
				reader.close();
			}
			
			return builder;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
}
