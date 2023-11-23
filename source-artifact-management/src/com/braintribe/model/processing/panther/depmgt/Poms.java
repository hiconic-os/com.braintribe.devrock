// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.panther.depmgt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiFunction;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.panther.SvnUtil;

public class Poms {
	private final static Logger logger = Logger.getLogger(Poms.class);
	private final static XMLInputFactory inputFactory;
	private final static Collection<String> propertiesPath = Arrays.asList("properties", "project");
	private final static Collection<String> versionPath = Arrays.asList("version", "project");
	
	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = logger.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch(Exception e) {
			if (debug) logger.debug("Could not set feature "+XMLInputFactory.SUPPORT_DTD+"=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch(Exception e) {
			if (debug) logger.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
	}

	
	public static <T> T readProperties(File file, BiFunction<String, String, T> listener) throws Exception {
		try (InputStream in = new FileInputStream(file)) {
			return readProperties(in, listener);
		}
	}
	
	public static <T> T readProperties(InputStream in, BiFunction<String, String, T> listener) throws Exception {
		XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
		T result = null;
		try {
			Deque<String> stack = new ArrayDeque<String>();
			String property = null;
			StringBuilder propertyValue = null;
			
			
			loop: while (reader.hasNext()){
				reader.next();
				switch (reader.getEventType()) {
				
				case XMLStreamReader.START_ELEMENT:
					String name = reader.getLocalName();

					if (equals(stack, propertiesPath)) {
						property = name;
						propertyValue = new StringBuilder();
					}
					
					stack.push(name);
					break;
					
				case XMLStreamReader.END_ELEMENT:
					if (property != null && stack.size() == 3) {
						result = listener.apply(property, propertyValue.toString());
						if (result != null) {
							break loop;
						}
						property = null;
						propertyValue = null;
					}
					else if (equals(stack, propertiesPath)) {
						break loop;
					}
						
					stack.pop();
					break;
					
				case XMLStreamReader.CHARACTERS:
				case XMLStreamReader.SPACE:
					if (property != null) {
						propertyValue.append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					}
					break;
					
				case XMLStreamReader.END_DOCUMENT:
					break;
				}
	
			}
		}
		finally {
			reader.close();
		}

		
		return result;
	}
	
	public static String readVersion(File file) throws Exception {
		try (InputStream in = new FileInputStream(file)) {
			return readVersion(in);
		}
	}
	
	public static String readVersion(InputStream in) throws Exception {
		XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
		String result = null;
		try {
			Deque<String> stack = new ArrayDeque<String>();
			StringBuilder versionBuilder = null;
			
			loop: while (reader.hasNext()){
				reader.next();
				switch (reader.getEventType()) {
				
				case XMLStreamReader.START_ELEMENT:
					String name = reader.getLocalName();
					stack.push(name);
					
					if (equals(stack, versionPath)) {
						versionBuilder  = new StringBuilder();
					}
					break;
					
				case XMLStreamReader.END_ELEMENT:
					if (versionBuilder != null && stack.size() == 2) {
						result = versionBuilder.toString();
						break loop;
					}
					stack.pop();
					break;
					
				case XMLStreamReader.CHARACTERS:
				case XMLStreamReader.SPACE:
					if (versionBuilder != null) {
						versionBuilder.append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					}
					break;
					
				case XMLStreamReader.END_DOCUMENT:
					break;
				}
				
			}
		}
		finally {
			reader.close();
		}
		
		
		return result;
	}
	
	public static void main(String[] args) {
		
		try {
			String fileName = "C:\\svn\\artifacts\\com\\braintribe\\FilterApi\\1.1\\pom.xml";
			String url = "https://svn.braintribe.com/repo/master/Development/artifacts/com/braintribe/FilterApi/1.1/pom.xml";
			
			try (InputStream in = SvnUtil.streamedCat(url, "HEAD")) {
				String value = readProperties(in, (p, v) -> p.equals("codebase")? v: null);
				System.out.println(value);
			}
			
			try (InputStream in = SvnUtil.streamedCat(url, "HEAD")) {
				String version = readVersion(in);
				System.out.println(version);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean equals(Collection<?> d1, Collection<?> d2) {
		int s1 = d1.size();
		int s2 = d2.size();
		
		if (s1 != s2)
			return false;
		
		Iterator<?> i1 = d1.iterator();
		Iterator<?> i2 = d2.iterator();
		
		while (i1.hasNext()) {
			Object o1 = i1.next();
			Object o2 = i2.next();
			
			if (!o1.equals(o2))
				return false;
		}
		
		return true;
	}
}
