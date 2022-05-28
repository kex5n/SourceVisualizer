package model.domain;

public class Method extends Attribute {
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
}