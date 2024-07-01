// ============================================================================
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
package com.braintribe.devrock.zarathud.extracter.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.zarathud.extracter.filter.ArtifactMatcher;
import com.braintribe.devrock.zarathud.extracter.filter.StandardFilter;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.AnnotationValueContainer;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

public class DependencyExtracter {
	
	private Set<AbstractEntity> processedEntities;
	
	/**
	 * collect actual dependencies from the artifact by traversing the artifact's entries
	 * @param artifact - the {@link Artifact} to retrieve it from 
	 * @return - a {@link List} of {@link Artifact} (that are not the main one)
	 */
	public List<Artifact> collectDependencies(Artifact artifact) {
		processedEntities = new HashSet<>();
		
		Predicate<AbstractEntity> entityFilter = new StandardFilter(artifact);
		Predicate<Artifact> artifactFilter = a -> !ArtifactMatcher.matchArtifactTo(artifact, a);
		
		Set<Artifact> referencedArtifacts = new HashSet<>();
		artifact.getEntries().stream().forEach( e -> {
			if (! processedEntities.contains( e)) {
				processedEntities.add(e);
				if (entityFilter.test(e)) {
					List<Artifact> dependencies = getAllDependencies( e);
					referencedArtifacts.addAll( dependencies.stream().filter( artifactFilter).collect(Collectors.toSet()));
				}
			}
		});
		 		
		return new ArrayList<Artifact>( referencedArtifacts);
	}
	
	/**
	 * return all {@link Artifact} of the {@link AbstractEntity}
	 * @param e - the {@link AbstractEntity}
	 * @return - a {@link List} of found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( AbstractEntity e) {
		List<Artifact> result = new ArrayList<>();
		// 
		if (e instanceof InterfaceEntity) {
			result.addAll( getAllDependencies((AbstractClassEntity) e));			 		
			result.addAll( getAllDependencies( (InterfaceEntity) e));			
		}
		else if (e instanceof ClassEntity) {
			result.addAll( getAllDependencies((AbstractClassEntity) e));
			result.addAll( getAllDependencies( (ClassEntity) e));
		}
		else if (e instanceof AbstractClassEntity) {
			result.addAll( getAllDependencies( (AbstractClassEntity) e));
		}
		
		result.add( e.getArtifact());
	
		return result;
	}
	
	/**
	 * extracts the artifact from the super type and implemented interfaces,
	 * and fields
	 * @param e - the {@link ClassEntity}
	 * @return - a {@link List} of found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( ClassEntity e) {
		List<Artifact> result = new ArrayList<>();
		// super type
		ClassEntity superType = e.getSuperType();
		if (superType != null) {
			Artifact superTypeArtifact = superType.getArtifact();
			if (superTypeArtifact != null) {
				result.add( superTypeArtifact);
			}
		}
		// interfaces 
		e.getImplementedInterfaces().stream().forEach( i -> {
			Artifact artifact = i.getArtifact();
			if (artifact != null) {
				result.add( artifact);
			}
		});
		// fields
		e.getFields().stream().forEach( f -> {
			AbstractEntity type = f.getType();
			if (type != null) {				
				result.addAll( getAllDependencies( type));				
			}
		});
		return result;
	}
	
	/**
	 * extracts the artifact from all super interfaces 
	 * @param e
	 * @return - a {@link List} of found {@link Artifact}	 
	 */
	private List<Artifact> getAllDependencies( InterfaceEntity e) {
		List<Artifact> result = new ArrayList<>();
		e.getSuperInterfaces().stream().forEach( i -> {
			Artifact artifact = i.getArtifact();
			if (artifact != null) {
				result.add( artifact);
			}
		});
		return result;
	}
	
	
	/**
	 * extract the {@link Artifact} from interface and class,
	 * i.e. from methods and annotations 
	 * @param e - the {@link AbstractEntity}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( AbstractClassEntity e) {
		List<Artifact> result = new ArrayList<>();
		e.getAnnotations().stream().forEach( a -> {
			result.addAll( getAllDependencies( a));
		});
		e.getMethods().stream().forEach( m -> {
			result.addAll( getAllDependencies( m));
		});
		e.getParameterization().forEach( p -> {
			result.addAll( getAllDependencies(p));
		});
		return result;
	}
	

	/**
	 * extract the {@link Artifact} information from an {@link AnnotationEntity}
	 * @param e - the {@link AnnotationEntity}
 	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( AnnotationEntity e) {
		List<Artifact> result = new ArrayList<>();
		result.add( e.getArtifact());
		for (Entry<String, AnnotationValueContainer> entry : e.getMembers().entrySet()) {
				result.addAll( getAllDependencies( entry.getValue()));
		}
		return result;
	}
	
	/**
	 * extract the {@link Artifact} information from an {@link AnnotationValueContainer}
	 * @param c - the {@link AnnotationValueContainer}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( AnnotationValueContainer c) {
		List<Artifact> result = new ArrayList<>();
		switch (c.getContainerType()) {
			case annotation:
				result.add( c.getAnnotation().getArtifact());
				break;
			case collection:
				c.getChildren().stream().forEach( g -> result.addAll( getAllDependencies( g)));
				break;
			default:
				break;
		}
		return result;
	}
	
	/**
	 * extract the {@link Artifact} from a {@link MethodEntity}, ie.e arguments, return type and exceptions
	 * @param m - the {@link MethodEntity}
	 * @return - a {@link List} of all found {@link Artifact}
	 */
	private List<Artifact> getAllDependencies( MethodEntity m) {
		List<Artifact> result = new ArrayList<>();
		m.getArgumentTypes().stream().forEach( t -> {
			if (t != null)
				result.addAll( getAllDependencies(t));
		});
		
		AbstractEntity resolvedReturnType = m.getReturnType();
		if (resolvedReturnType !=  null) {
			result.addAll( getAllDependencies(resolvedReturnType));			
		}
		// 
		 
		m.getExceptions().stream().forEach( x -> result.add(x.getArtifact()));
		
		return result;
	}

	
	
}
