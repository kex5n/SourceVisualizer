package model.domain;

public class Dependency {
	private Attribute src;
	private Attribute dst;

	public Dependency(Attribute src, Attribute dst) {
		this.src = src;
		this.dst = dst;
	}
	
	public Attribute getSrc() {
		return src;
	}

	public Attribute getDst() {
		return dst;
	}
}
