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

	private final boolean forNpm;
	private final String npmNamespace;	
	private final GmMetaModel model;
	private final String gid;
	private final String aid;
	private final String version;
	private final List<VersionedArtifactIdentification> dependencies;
	private final ShortNames<GmType> shortNames;
	private final String fileNameBase;

	public ModelEnsuringContext(boolean forNpm, String npmNamespace, GmMetaModel model, String gid, String aid, String version, List<VersionedArtifactIdentification> dependencies) {
		this.forNpm = forNpm;
		this.npmNamespace = npmNamespace;
		this.model = model;
		this.gid = gid;
		this.aid = aid;
		this.version = version;
		this.dependencies = dependencies;
		this.shortNames = NameShortener.shortenNames(model.getTypes(), GmType::getTypeSignature);
		this.fileNameBase = forNpm ? aid : TypeScriptWriterHelper.nameBaseOfEnsure(aid);
	}

	// @formatter:off
	public boolean forNpm() { return forNpm; }
	public String npmNamespace() {return npmNamespace; }
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

		return new ModelEnsuringContext(false, null, model, vai.getGroupId(), vai.getArtifactId(), vai.getVersion(), deps);
	}

	private static List<VersionedArtifactIdentification> getDependencyIdentifications(GmMetaModel model, Function<String, String> versionRangifier) {
		return model.getDependencies().stream() //
				.map(m -> TypeScriptWriterHelper.modelToArtifactInfo(m, versionRangifier)) //
				.collect(Collectors.toList());
	}

	public static ModelEnsuringContext create( //
			List<GmType> gmTypes, String gid, String aid, String version, List<VersionedArtifactIdentification> dependencies) {

		GmMetaModel model = toModel(gmTypes, gid, aid, version);
		return new ModelEnsuringContext(false, null, model, gid, aid, version, dependencies);
	}

	public static ModelEnsuringContext createForNpm( String npmNamespace, //
			List<GmType> gmTypes, String gid, String aid, String version, List<? extends VersionedArtifactIdentification> dependencies) {

		GmMetaModel model = toModel(gmTypes, gid, aid, version);
		List<VersionedArtifactIdentification> deps = (List<VersionedArtifactIdentification>) dependencies;
		return new ModelEnsuringContext(true, npmNamespace, model, gid, aid, version, deps);
	}

	private static GmMetaModel toModel(Collection<GmType> gmTypes, String gid, String aid, String v) {
		GmMetaModel model = GmMetaModel.T.create();
		model.setName(gid + ":" + aid);
		model.setVersion(v);
		model.setTypes(newSet(gmTypes));
		return model;
	}

}
