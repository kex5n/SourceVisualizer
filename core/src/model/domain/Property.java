package model.domain;

public class Property extends Attribute {
	private String name;
	private String type;

	public Property(String name, String type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}
}
