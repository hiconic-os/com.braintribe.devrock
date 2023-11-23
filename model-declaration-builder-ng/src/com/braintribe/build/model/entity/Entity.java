package com.braintribe.build.model.entity;

import java.util.List;

/**
 * common entity for both reflections 
 * @author pit
 */
public class Entity {		
	private boolean genericEntity;
	private String forward;
	private boolean isEnum;
	private String name;
	private List<String> interfaces;
	private String superType;
	
	public Entity() {}
	
	public Entity(String name, boolean isGeneric, boolean isEnum, String forward) {		
		this.genericEntity = isGeneric;
		this.isEnum = isEnum;
		this.forward = forward;	
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getForwardDeclaration() {
		return forward;
	}
	public void setForwardDeclaration(String forward) {
		this.forward = forward;
	}
	
	public boolean getIsGenericEntity() {
		return genericEntity;
	}

	public void setIsGenericEntity(boolean genericEntity) {
		this.genericEntity = genericEntity;
	}

	public void setIsEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}	
	public boolean getIsEnum() {	
		return isEnum;
	}

	/**
	 * only required/non-null in ASM reflection
	 * @return - the list of implemented interfaces if any 
	 */
	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}
	
	/**
	 * only required/non-null in ASM reflection 
	 * @return - the super type if any 
	 */
	public String getSuperType() {
		return superType;
	}

	public void setSuperType(String superType) {
		this.superType = superType;
	}
	
	
	
	
	
}
