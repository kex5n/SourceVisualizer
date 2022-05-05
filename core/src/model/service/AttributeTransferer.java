package model.service;

import model.domain.Class;
import model.domain.Method;
import model.domain.Attribute;

public class AttributeTransferer {
	Class leftClass;
	Class rightClass;

	public AttributeTransferer(Class leftClass, Class rightClass){
		this.leftClass = leftClass;
		this.rightClass = rightClass;
	}

	public boolean leftClassHas(String methodName) {
		return leftClass.has(methodName);
	}

	public Class getLeftClass() {
		return leftClass;
	}

	public Class getRightClass() {
		return rightClass;
	}
	
	public void transferAttribute(String attributeName) {
		if (leftClassHas(attributeName)) {
			Attribute removedAttribute = leftClass.removeAttribute(attributeName);
			rightClass.setAttribute(removedAttribute);
		} else {
			Attribute removedAttribute = rightClass.removeAttribute(attributeName);
			leftClass.setAttribute(removedAttribute);
		}
	}
}
