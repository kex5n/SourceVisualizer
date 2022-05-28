package model.domain;

import model.service.StringSort;

public class Method extends Attribute implements Comparable<Method> {
	public Method() {};
	public Method(String name, int argcnt) {
		this.name = name;
		this.argcnt = argcnt;
	};

	public Method(String name) {
		this.name = name;
		this.argcnt = 0;
	}

	private String name;
	private int argcnt;

	@Override
	public String getName() {
		return name;
	}

	public int getArgcnt() {
		return argcnt;
	}
	public String toString() {
		return "name: " + name + ", argcnt: " + argcnt;
	}

	public int hashCode() {
		return name.hashCode() + argcnt;
	}
	
	@Override
	public int compareTo(Method m){
		return StringSort.compareStrings(this.getName(), m.getName());
	}

	public Method clone() {
		return new Method(this.name, this.argcnt);
	}
}