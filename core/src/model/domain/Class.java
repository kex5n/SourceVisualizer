package model.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Class extends Module {
	public Class() {};
	public Class(
			String name, ArrayList<Property> properties, ArrayList<Method> methods) {
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
		this.dependencies = new ArrayList<Dependency>();
	};

	private String name;
	private ArrayList<Property> properties;
	private ArrayList<Method> methods;
	private ArrayList<Dependency> dependencies;
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

	public void removeDependency(Dependency d) {
		dependencies.remove(d);
	}

	public void setAttribute(Attribute attribute) {
		if (attribute instanceof Method) {
			methods.add((Method) attribute);
		} else {
			properties.add((Property) attribute);
		}
		attributeMap.put(attribute.getName(), attribute);
	}
	
	public ArrayList<Property> getProperties(){
		return properties;
	}

	public ArrayList<Method> getMethods() {
		return methods;
	}

	public Attribute getAttribute(String attributeName) {
		return attributeMap.get(attributeName);
	}

	public void setDependencies(String src, String dst) throws Exception {
		Attribute srcAttribute = attributeMap.get(src);
		Attribute dstAttribute = attributeMap.get(dst);

		if ((srcAttribute == null) | (dstAttribute == null)) {
			throw new Exception("Attribute isn't found.");
		}
		Dependency dependency = new Dependency(srcAttribute, dstAttribute);
		dependencies.add(dependency);
	}
	
	public ArrayList<Dependency> getDependencies(){
		return dependencies;
	}

	public ArrayList<Dependency> getRelatedDependencies(Attribute a){
		ArrayList<Dependency> allDependencies = getDependencies();
		ArrayList<Dependency> relatedDependencies = new ArrayList<Dependency>();
		for (Dependency d: allDependencies) {
			if (d.getSrc().getName() == a.getName()) {
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
}