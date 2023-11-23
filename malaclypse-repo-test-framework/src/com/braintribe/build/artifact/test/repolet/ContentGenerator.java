// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
	public String render( String path, List<String> files) throws VelocityTemplateRendererException{
		if (path.indexOf('/') > 0) {
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
			tuple = new Tuple( file, file);			
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
