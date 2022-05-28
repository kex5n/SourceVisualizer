package model.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Class extends Module {
	public Class() {};
	public Class(
			String name, HashSet<Property> properties, HashSet<Method> methods) {
		this.name = name;
		this.properties = properties;
		this.methods = methods;
		this.attributeMap = new HashMap<String, Attribute>();
		for (Property p: properties) {
			this.attributeMap.put(p.getName(), p);
		}
		for (Method m: methods) {
			this.attributeMap.put(m.getName(), m);
		}
		this.internalDependencies = new ArrayList<InternalDependency>();
		this.externalDependencies = new ArrayList<ExternalDependency>();
	};
	public Class(String name) {
		this.name = name;
		this.properties = new HashSet<Property>();
		this.methods = new HashSet<Method>();
		this.internalDependencies = new ArrayList<InternalDependency>();
		this.externalDependencies = new ArrayList<ExternalDependency>();
		this.attributeMap = new HashMap<String, Attribute>();
	}

	private String name;
	private HashSet<Property> properties;
	private HashSet<Method> methods;
	private ArrayList<InternalDependency> internalDependencies;
	private ArrayList<ExternalDependency> externalDependencies;
	private HashMap<String, Attribute> attributeMap;

	@Override
	public String getName() {
		return name;
	}

	public boolean has(String attributeName) {
		return attributeMap.containsKey(attributeName);
	}

	public Attribute removeAttribute(String attributeName) {
		Attribute removedAttribute = attributeMap.get(attributeName);
		if (removedAttribute instanceof Method) {
			methods.remove(removedAttribute);
		} else {
			properties.remove(removedAttribute);
		}
		return attributeMap.remove(attributeName);
	}

	public void removeInternalDependency(InternalDependency d) {
		internalDependencies.remove(d);
	}

	public void removeExternalDependency(ExternalDependency d) {
		externalDependencies.remove(d);
	}

	public void setAttribute(Attribute attribute) {
		if (attribute instanceof Method) {
			methods.add((Method) attribute);
		} else {
			properties.add((Property) attribute);
		}
		attributeMap.put(attribute.getName(), attribute);
	}
	
	public HashSet<Property> getProperties(){
		return properties;
	}

	public HashSet<Method> getMethods() {
		return methods;
	}

	public Attribute getAttribute(String attributeName) {
		return attributeMap.get(attributeName);
	}

	public void setInternalDependencies(String srcName, String dstName) throws Exception {
		if (!(attributeMap.keySet().contains(srcName)) | !(attributeMap.keySet().contains(dstName))) {
			throw new Exception("Attribute isn't found.");
		}
		InternalDependency internalDependency = new InternalDependency(srcName, dstName);
		internalDependencies.add(internalDependency);
	}

	public void setExternalDependencies(String srcName, Class dstClass, String dstName) throws Exception {
		if (!(attributeMap.keySet().contains(srcName)) | (dstClass.getAttribute(dstName) == null)) {
			throw new Exception("Attribute isn't found.");
		}
		ExternalDependency externalDependency = new ExternalDependency(srcName, dstClass.getName(), dstName);
		externalDependencies.add(externalDependency);
	}

	public ArrayList<ExternalDependency> getExternalDependencies() {
		return externalDependencies;
	}

	public ArrayList<InternalDependency> getInternalDependencies(){
		return internalDependencies;
	}

	public ArrayList<InternalDependency> getSrcInternalDependencies(Attribute a){
		// 内部依存関係のうち、移動するAttributeがsrcのものを取得する。
		ArrayList<InternalDependency> allDependencies = getInternalDependencies();
		ArrayList<InternalDependency> relatedDependencies = new ArrayList<InternalDependency>();
		for (InternalDependency d: allDependencies) {
			String dependencySrcName = d.getSrcName();
			String attributeSrcName = a.getName();
			if (dependencySrcName.equals(attributeSrcName)) {
				relatedDependencies.add(d);
			}
		}
		return relatedDependencies;
	}

	public ArrayList<InternalDependency> getDstInternalDependencies(Attribute a){
		// 内部依存関係のうち、移動するAttributeがdstのものを取得する。
		ArrayList<InternalDependency> allDependencies = getInternalDependencies();
		ArrayList<InternalDependency> relatedDependencies = new ArrayList<InternalDependency>();
		for (InternalDependency d: allDependencies) {
			if (d.getDstName() == a.getName()) {
				relatedDependencies.add(d);
			}
		}
		return relatedDependencies;
	}

	public String toString() {
		String message = "name: " + name + ", methods: ";
		for (Method method : methods) {
			message += method.getName() + ",";
		}
		return message;
	}

	public Class clone() {
		Class c = new Class(this.name, (HashSet<Property>) this.properties.clone(), (HashSet<Method>) this.methods.clone());
		return c;
	}
}