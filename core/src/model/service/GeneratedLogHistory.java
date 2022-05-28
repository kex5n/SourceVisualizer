package model.service;

import model.domain.Attribute;
import model.domain.Class;

public class GeneratedLogHistory {
	private Class srcClass;
	private Class dstClass;
	private Attribute srcAttribute;
	private Attribute dstAttribute;

	public GeneratedLogHistory(Class srcClass, Class dstClass, Attribute srcAttribute, Attribute dstAttribute) {
		this.srcClass = srcClass;
		this.dstClass = dstClass;
		this.srcAttribute = srcAttribute;
		this.dstAttribute = dstAttribute;
	}

	public String getSrcClassName() {
		return srcClass.getName();
	}

	public String getDstClassName() {
		return dstClass.getName();
	}

	public String getSrcAttributeName() {
		return srcAttribute.getName();
	}

	public String getDstAttributeName() {
		return dstAttribute.getName();
	}
}
