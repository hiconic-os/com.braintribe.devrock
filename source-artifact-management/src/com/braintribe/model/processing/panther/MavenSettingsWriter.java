// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.model.processing.panther;

import java.io.File;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.logging.Logger;
import com.braintribe.model.panther.ArtifactRepository;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.utils.xml.XmlTools;

public class MavenSettingsWriter {
	private static final Logger logger = Logger.getLogger(MavenSettingsWriter.class);
	private static VelocityEngine velocityEngine;

	static {
		try {
			Properties properties = new Properties();
			URL templateUrl = MavenSettingsWriter.class.getResource("settings.xml.vm");
			URL baseUrl = new URL(templateUrl, ".");

			properties.setProperty("input.encoding", "UTF-8");
			properties.setProperty("resource.loaders", "url");
			properties.setProperty("url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader");

			properties.setProperty("url.resource.loader.root", baseUrl.toString());
			properties.setProperty("url.resource.loader.cache", "false");

			velocityEngine = new VelocityEngine();
			velocityEngine.init(properties);
		} catch (MalformedURLException e) {
			logger.error("Error while initializing velocity engine", e);
		}
	}

	public static void write(SourceRepository sourceRepository, File localRepository, String centralMirrorUrl, Writer writer) {
		VelocityContext context = new VelocityContext();
		context.put("localrepo", localRepository.getAbsolutePath());
		context.put("repos", sourceRepository.getLookupRepositories());
		context.put("updateReflectionRepositories", sourceRepository.getLookupRepositories().stream().filter(r -> r.getUpdateReflectionUrl() != null)
				.map(ArtifactRepository::getName).collect(Collectors.joining(",")));
		context.put("centralMirrorUrl", centralMirrorUrl);
		context.put("tools", Tool.INSTANCE);
		velocityEngine.mergeTemplate("settings.xml.vm", "UTF-8", context, writer);
	}

	public static class Tool {
		private static Tool INSTANCE = new Tool();

		public String esc(String s) {
			if (s == null)
				return null;
			return XmlTools.escape(s);
		}
	}
}
