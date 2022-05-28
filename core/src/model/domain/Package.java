package model.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class Package extends Module {
	public Package() {};
	public Package(String name, ArrayList<Class> classes) {
		this.name = name;
		this.classes = classes;
		this.classesMap = new HashMap<String, Class>();
		for (Class c: classes) {
			this.classesMap.put(c.getName(), c);
		}
	};

	private String name;
	private ArrayList<Class> classes;
	private HashMap<String, Class> classesMap;
	
	@Override
	public String getName() {
		return name;
	};

	public Class getClass(String className) {
		return classesMap.get(className);
	}

	public void setClass(Class c) {
		classes.add(c);
		classesMap.put(c.getName(), c);
	}

	public ArrayList<Class> getClasses() {
		return classes;
	}

	public String toString() {
		String message = "name: " + name + ", contents: ";
		for (Class c : classes) {
			message += c.getName() + ",";
		}
		return message;
	}

	public Package clone() {
		ArrayList<Class> classArray = new ArrayList<Class>();
		for (Class c: classes) {
			classArray.add(c.clone());
		}
		return new Package(name, classArray);
	}
}