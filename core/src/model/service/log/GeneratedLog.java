package model.service.log;

import model.domain.Attribute;
import model.domain.Class;

public class GeneratedLog {
	private String srcClassName;
	private String dstClassName;
	private String srcAttributeName;
	private String dstAttributeName;

	public GeneratedLog(
		String srcClassName,
		String dstClassName,
		String srcAttributeName,
		String dstAttributeName
	) {
		this.srcClassName = srcClassName;
		this.dstClassName = dstClassName;
		this.srcAttributeName = srcAttributeName;
		this.dstAttributeName = dstAttributeName;
	}

	public String getSrcClassName() {
		return srcClassName;
	}

	public String getDstClassName() {
		return dstClassName;
	}

	public String getSrcAttributeName() {
		return srcAttributeName;
	}

	public String getDstAttributeName() {
		return dstAttributeName;
	}
}
