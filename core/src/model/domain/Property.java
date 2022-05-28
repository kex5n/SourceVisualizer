package model.domain;

import model.service.StringSort;

public class Property extends Attribute implements Comparable<Property> {
	private String name;
	private String type;

	public Property(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public int hashCode() {
		return name.hashCode() + type.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}

	public Property clone() {
		return new Property(this.name, this.type);
	}

	@Override
	public int compareTo(Property p){
		return StringSort.compareStrings(this.getName(), p.getName());
	}
	
}
