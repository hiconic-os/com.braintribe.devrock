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
package com.braintribe.devrock.repolet.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.web.velocity.renderer.VelocityTemplateRenderer;
import com.braintribe.web.velocity.renderer.VelocityTemplateRendererException;

/**
 * actually create the html output for the repolet 
 * @author pit
 *
 */
public class ContentGenerator  {
	private VelocityTemplateRenderer renderer;
	
	private String collection_template =  "template/collection.vm";
	private String nothing_found_template = "template/404.vm";
	

	public ContentGenerator() {
		renderer = new VelocityTemplateRenderer();
		Map<String, Supplier<String>> providerMap = new HashMap<String, Supplier<String>>();
			
		providerMap.put( "collection_content", new StreambasedTemplateProvider( this.getClass().getResourceAsStream( collection_template)));
		providerMap.put( "no_content", new StreambasedTemplateProvider( this.getClass().getResourceAsStream( nothing_found_template)));
		renderer.setKeyToProviderMap( providerMap);
		
	}
	/**
	 * renders a 'directory' listing
	 * @param root - the root (url prefix)
	 * @param path - the path to render 
	 * @param files - the content of the path 
	 * @return - the rendered string 
	 * @throws VelocityTemplateRendererException
	 */
	public String render( String root, String path, List<String> files) throws VelocityTemplateRendererException{		
		String subpath =  path.substring( root.length()+1);
		if (subpath.length() > 1 && subpath.endsWith("/")) {
			String parent = path.substring(0, path.lastIndexOf('/'));
			String rel = path.substring( path.lastIndexOf( '/')+1);
			renderer.setContextValue( "contents", "parent", parent);
			renderer.setContextValue( "contents", "rel", rel);
		}
		else {
			renderer.setContextValue( "contents", "parent", null);
		}
		renderer.setContextValue( "contents", "group", path);
		List<Tuple> tuples = new ArrayList<Tuple>();
		for (String file : files) {
			Tuple tuple;
			tuple = new Tuple( path + (path.endsWith( "/") ? file : "/" + file), file);			
			tuples.add( tuple);
		}
		renderer.setContextValue( "contents", "files", tuples);	
		return renderer.renderTemplate("collection_content", "contents");
	}
	
	public String descriptiveRender( String root, String path, List<Pair<String,String>> files) throws VelocityTemplateRendererException{				
		if (path.length() > 1) {
			String parent = path.substring(0, path.lastIndexOf('/'));
			String rel = path.substring( path.lastIndexOf( '/')+1);
			renderer.setContextValue( "contents", "parent", parent.startsWith("/") ? root + parent : root + "/" + parent);
			renderer.setContextValue( "contents", "rel", rel);
		}
		else {
			renderer.setContextValue( "contents", "parent", null);
		}
		renderer.setContextValue( "contents", "group", path);
		List<Tuple> tuples = new ArrayList<Tuple>();
		for (Pair<String,String> file : files) {
			Tuple tuple;
			tuple = new Tuple( file.first, file.second);			
			tuples.add( tuple);
		}
		renderer.setContextValue( "contents", "files", tuples);	
		return renderer.renderTemplate("collection_content", "contents");
	}
	
	
	public String render404( String path) throws VelocityTemplateRendererException {
	
		renderer.setContextValue( "404", "path", path);		
		return renderer.renderTemplate("no_content", "404");
	}
	
	
	public class Tuple {
		private String href;
		private String name;
		
		public Tuple( String href, String name) {
			this.href = href;
			this.name = name;		
		}

		public String getHref() {
			return href;
		}

		public String getName() {
			return name;
		}				
	}
}
