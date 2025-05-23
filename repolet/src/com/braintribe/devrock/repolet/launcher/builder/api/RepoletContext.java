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
package com.braintribe.devrock.repolet.launcher.builder.api;

import java.io.File;
import java.util.Date;

import com.braintribe.devrock.repolet.launcher.builder.cfg.DescriptiveContentCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.FilesystemCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.HashCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedDescriptiveContentCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.IndexedFilesystemCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.UploadReturnValueOverrideCfg;

/**
 * a builder context for a repolet configuration 
 * @author pit
 *
 * @param <T> - the 'owning' type, i.e. the type that implements {@link RepoletCfgConsumer}
 */
public class RepoletContext<T extends RepoletCfgConsumer> implements 	FilesystemCfgConsumer,
																		IndexedFilesystemConsumer, 
																		UploadFilesystemCfgConsumer, 
																		DescriptiveContentCfgConsumer,
																		IndexedDescriptiveContentConsumer,
																		HashOverrideConsumer,
																		UploadReturnValueOverrideConsumer
																		{
		
	private T consumer;
	private RepoletCfg cfg;
	
	/**
	 * @param consumer - the {@link RepoletCfgConsumer}, i.e the one that receives its contained cfg
	 */
	public RepoletContext(T consumer) {
		this.consumer = consumer;
		cfg = new RepoletCfg();
	}
	
	@Override
	public void accept(FilesystemCfg t) {
		cfg.getFilesystems().add( t);
	}
	
	
	@Override
	public void accept(IndexedFilesystemCfg t) {
		cfg.getIndexedFilesystems().add( t);		
	}

	@Override
	public void acceptForUpload(FilesystemCfg t) {
		cfg.setUploadFilesystem( t);		
	}

	@Override
	public void accept(DescriptiveContentCfg t) {
		cfg.getDescriptiveContentDescriptions().add( t);
	}

	@Override
	public void accept(IndexedDescriptiveContentCfg t) {
		cfg.getIndexedDescriptiveContentDescriptions().add( t);
		
	}

	@Override
	public void accept(HashCfg t) {
		cfg.getHashOverrides().put( t.getNode(), t.getHashes());
		if (t.getNoHeaders()) {
			cfg.getNoHashesInHeader().put( t.getNode(), true);
		}
	}
	
	@Override
	public void accept(UploadReturnValueOverrideCfg t) {
		cfg.getUploadReturnValuesOverride().putAll( t.getUploadReturnValuesOverride());		
	}

	/**
	 * @return - a {@link FilesystemContext}
	 */
	public FilesystemContext<RepoletContext<T>> filesystem() {
		return new FilesystemContext<>( this);
	}
	
	/**
	 * @return - a {@link FilesystemContext}
	 */
	public UploadFilesystemContext<RepoletContext<T>> uploadFilesystem() {
		return new UploadFilesystemContext<>( this);
	}
	
	/**
	 * @return - a {@link IndexedFilesystemContext}
	 */
	public IndexedFilesystemContext<RepoletContext<T>> indexedFilesystem() {
		return new IndexedFilesystemContext<>( this);
	}
	
	public DescriptiveContentContext<RepoletContext<T>> descriptiveContent() {
		return new DescriptiveContentContext<>( this);
	}
	
	public IndexedDescriptiveContentContext<RepoletContext<T>> indexedDescriptiveContent() {
		return new IndexedDescriptiveContentContext<>( this);
	}
	
	public HashOverrideContext<RepoletContext<T>> hashes(String node) {		
		return new HashOverrideContext<>(this, node);
	}
	
	public UploadReturnValueOverrideContext<RepoletContext<T>> uploadReturnValueOverrides() {
		return new UploadReturnValueOverrideContext<>( this);
	}
	
	
	/**
	 * @param name - the name of the repolet (making up the base url)
	 * @return - itself
	 */
	public RepoletContext<T> name(String name) {
		cfg.setName(name);
		return this;
	}	
	
	/**
	 * @param code - the overriding response code for all repolet actions 
	 * @return - itself
	 */
	public RepoletContext<T> overridingResponseCode(int code) {
		cfg.setOverridingReponseCode( code);
		return this;
	}
	
	
	/**
	 * @param serverIdentification - the name (id) of the server to be returned at probing
	 * @return - itself
	 */
	public RepoletContext<T> serverIdentification( String serverIdentification) {
		cfg.setServerIdentification(serverIdentification);
		return this;
	}
	
	/**
	 * @param changesUrl - the url the repolet is responding for RH requests
	 * @return - itself
	 */
	public RepoletContext<T> changesUrl( String changesUrl) {
		cfg.setChangesUrl(changesUrl);
		return this;
	}
	
	/**
	 * @param restApiUrl
	 * @return
	 */
	public RepoletContext<T> restApiUrl( String restApiUrl) {
		cfg.setRestApiUrl(restApiUrl);
		return this;
	}

	
	public RepoletContext<T> changes( Date date, File response) {
		cfg.getDateToContentFile().put( date, response);
		return this;
	}
	
	/**
	 * @return - the consumer, ie. the owner
	 */
	public T close() {
		consumer.accept(cfg);
		return consumer;
	}
	
	
}
