package view.components;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassViewComposer {
	private int id;
	private ClassBox classBox;
	private HashMap<String, MethodBox> methodBoxMap;
	private HashMap<String, PropertyBox> propertyBoxMap;
	private ArrayList<DependencyVector> internalDependencyVectorArray;
	private ArrayList<DependencyVector> externalDependencyVectorArray;

	public ClassViewComposer(
		int id,
		ClassBox classBox,
		HashMap<String, MethodBox> methodBoxMap,
		HashMap<String, PropertyBox> propertyBoxMap,
		ArrayList<DependencyVector> internalDependencyVectorArray
	) {
		this.id = id;
		this.classBox = classBox;
		this.methodBoxMap = methodBoxMap;
		this.propertyBoxMap = propertyBoxMap;
		this.internalDependencyVectorArray = internalDependencyVectorArray;
		this.externalDependencyVectorArray = new ArrayList<DependencyVector>();
	}

	public int getId() {
		return id;
	}

	public ClassBox getClassBox() {
		return classBox;
	}
	public HashMap<String, MethodBox> getMethodBoxMap(){
		return methodBoxMap;
	}
	public HashMap<String, PropertyBox> getPropertyBoxMap(){
		return propertyBoxMap;
	}
	public ArrayList<DependencyVector> getInternalDependencyVectorArray(){
		return internalDependencyVectorArray;
	}
	public ArrayList<DependencyVector> getExternalDependencyVectorArray(){
		return externalDependencyVectorArray;
	}

	public void setExternalDependencyVectorArray(ArrayList<DependencyVector> externalDependencyVectorArray) {
		this.externalDependencyVectorArray = externalDependencyVectorArray;
	}
}
