package model.domain;

import java.util.ArrayList;

public class Package extends Module {
	public Package() {};
	public Package(String name, ArrayList<Class> classes) {
		this.name = name;
		this.classes = classes;
	};

	private String name;
	private ArrayList<Class> classes;

	@Override
	public String getName() {
		return name;
	};
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
}