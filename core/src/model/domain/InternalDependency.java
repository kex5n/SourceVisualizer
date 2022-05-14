package model.domain;

public class InternalDependency {
	private String srcName;
	private String dstName;

	public InternalDependency(String srcName, String dstName) {
		this.srcName = srcName;
		this.dstName = dstName;
	}
	
	public String getSrcName() {
		return srcName;
	}

	public String getDstName() {
		return dstName;
	}

	public boolean equals(Object other) {
		if (!(other instanceof InternalDependency)) {
			return false;
		}
		InternalDependency otherDependency = (InternalDependency) other;
		if ((this.getSrcName() == otherDependency.getSrcName()) & (this.getDstName() == otherDependency.getDstName())){
			return true;
		} else {
			return false;
		}
	}

	public int hashCode() {
        return this.getSrcName().hashCode() + this.getDstName().hashCode();
    }
}
