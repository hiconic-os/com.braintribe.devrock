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
package com.braintribe.model.typescript;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.shortener.NameShortener;
import com.braintribe.model.shortener.NameShortener.ShortNames;

/**
 * @author peter.gazdik
 */
public class ModelEnsuringContext {

	private final GmMetaModel model;
	private final String gid;
	private final String aid;
	private final String version;
	private final List<VersionedArtifactIdentification> dependencies;
	private final ShortNames<GmType> shortNames;
	private final String fileNameBase;

	public ModelEnsuringContext(GmMetaModel model, String gid, String aid, String version, List<VersionedArtifactIdentification> dependencies) {
		this.model = model;
		this.gid = gid;
		this.aid = aid;
		this.version = version;
		this.dependencies = dependencies;
		this.shortNames = NameShortener.shortenNames(model.getTypes(), GmType::getTypeSignature);
		this.fileNameBase = TypeScriptWriterHelper.nameBaseOfEnsure(aid);
	}

	// @formatter:off
	public GmMetaModel model() { return model; }
	public String aid() { return aid; }
	public String gid() { return gid; }
	public String version() { return version; }
	public List<VersionedArtifactIdentification> dependencies() { return dependencies; }
	public ShortNames<GmType> shortNames() { return shortNames; }
	public String dtsFileName() { return fileNameBase + ".d.ts"; } 
	public String jsFileName() { return fileNameBase + ".js"; } 
	// @formatter:on

	
	public static ModelEnsuringContext create(GmMetaModel model, Function<String, String> versionRangifier) {
		List<VersionedArtifactIdentification> deps = getDependencyIdentifications(model, versionRangifier);
		VersionedArtifactIdentification vai = TypeScriptWriterHelper.modelToArtifactInfo(model);

		return new ModelEnsuringContext(model, vai.getGroupId(), vai.getArtifactId(), vai.getVersion(), deps);
	}

	private static List<VersionedArtifactIdentification> getDependencyIdentifications(GmMetaModel model, Function<String, String> versionRangifier) {
		return model.getDependencies().stream() //
				.map(m -> TypeScriptWriterHelper.modelToArtifactInfo(m, versionRangifier)) //
				.collect(Collectors.toList());
	}

	public static ModelEnsuringContext create( //
			List<GmType> gmTypes, String gid, String aid, String version, List<VersionedArtifactIdentification> dependencies) {

		GmMetaModel model = toModel(gmTypes, gid, aid, version);
		return new ModelEnsuringContext(model, gid, aid, version, dependencies);
	}

	private static GmMetaModel toModel(Collection<GmType> gmTypes, String gid, String aid, String v) {
		GmMetaModel model = GmMetaModel.T.create();
		model.setName(gid + ":" + aid);
		model.setVersion(v);
		model.setTypes(newSet(gmTypes));
		return model;
	}

}
