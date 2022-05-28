package model.domain;

public class ExternalDependency {
	private String srcName;
	private String dstName;
	private String dstClassName;

	public ExternalDependency(String srcName, String dstClassName, String dstName) {
		this.dstClassName = dstClassName;
		this.srcName = srcName;
		this.dstName = dstName;
	}
	
	public String getSrcName() {
		return srcName;
	}

	public String getDstName() {
		return dstName;
	}

	public String dstClassName() {
		return dstClassName;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ExternalDependency)) {
			return false;
		}
		ExternalDependency otherDependency = (ExternalDependency) other;
		if (
				(this.getSrcName() == otherDependency.getSrcName())
				& (this.getDstName() == otherDependency.getDstName())
				& (this.getSrcName() == otherDependency.getSrcName())
		){
			return true;
		} else {
			return false;
		}
	}

	public int hashCode() {
        return this.getSrcName().hashCode() + this.getDstName().hashCode();
    }

	public ExternalDependency clone() {
		return new ExternalDependency(this.srcName, this.dstClassName, this.dstName);
	}
}
