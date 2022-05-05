package model.service;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import model.domain.Package;
import model.domain.Method;
import model.domain.Class;
import model.domain.Property;
import model.domain.Dependency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class PackageFactory {
	public static Package create() {
		ArrayList<Class> classArray = new ArrayList<Class>();

		JSONObject class1JsonObject = readJson("/home/kentaroishii/eclipse-workspace/sample/core/data/class1.json");
		Class class1 = deserializeClass(class1JsonObject);
		classArray.add(class1);

		JSONObject class2JsonObject = readJson("/home/kentaroishii/eclipse-workspace/sample/core/data/class2.json");
		Class class2 = deserializeClass(class2JsonObject);
		classArray.add(class2);

		Package p = new Package("package", classArray);
		return p;
	};

	private static JSONObject readJson(String filepath) {
		Object ob = new Object();
		try {
			ob = new JSONParser().parse(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			System.out.println("File isn't found.");
		} catch (ParseException e) {
			System.out.println("Error parse.");
		} catch (IOException e) {
			System.out.println("IOException occurred.");
		}
		JSONObject jo = (JSONObject) ob;
		return jo;
	}
	
	private static Class deserializeClass(JSONObject class_json) {
		String class_name = (String) class_json.get("classname");

		// Load Classed
		ArrayList<Property> class_property = new ArrayList<Property>();
		JSONArray property_json_array = (JSONArray) class_json.get("properties");
		for (Object property_ob : property_json_array) {
			JSONObject property_json = (JSONObject) property_ob;
			Property p = PackageFactory.deserializeProperty(property_json);
			class_property.add(p);
		}

		// Load Methods
		ArrayList<Method> class_methods = new ArrayList<Method>();
		JSONArray methods_json_array = (JSONArray) class_json.get("methods");
		for (Object method_ob : methods_json_array) {
			JSONObject method_json = (JSONObject) method_ob;
			Method m = PackageFactory.deserializeMethod(method_json);
			class_methods.add(m);
		}

		Class c = new Class(class_name, class_property, class_methods);

		// Set Dependencies
		JSONObject dependencies_json_object = (JSONObject) class_json.get("dependencies");

		// Load Method Dependencies
		JSONArray methodDependenciesJsonArray = (JSONArray) dependencies_json_object.get("mm");
		for (Object methodDependencyObject : methodDependenciesJsonArray) {
			JSONObject methodDependencyJsonObject = (JSONObject) methodDependencyObject;
			String src = (String) methodDependencyJsonObject.get("src");
			String dst = (String) methodDependencyJsonObject.get("dst");
			try {
				c.setDependencies(src, dst);
			} catch (Exception e) {
				System.exit(1);;
			}
		}
		// Load Property Dependencies
		JSONArray propertyDependenciesJsonArray = (JSONArray) dependencies_json_object.get("mv");
		for (Object propertyDependencyObject : propertyDependenciesJsonArray) {
			JSONObject propertyDependencyJsonObject = (JSONObject) propertyDependencyObject;
			String src = (String) propertyDependencyJsonObject.get("src");
			String dst = (String) propertyDependencyJsonObject.get("dst");
			try {
				c.setDependencies(src, dst);
			} catch (Exception e) {
				System.exit(1);;
			}
		}

		return c;
	}

	private static Property deserializeProperty(JSONObject property_json) {
		String property_name = (String) property_json.get("name");
		String type = (String) property_json.get("type");
		Property p = new Property(property_name, type);
		return p;
	}

	private static Method deserializeMethod(JSONObject method_json) {
		String method_name =  (String) method_json.get("name");
		long method_argcnt_long =  (long) method_json.get("argcnt");
		int method_argcnt = (int) method_argcnt_long;
		Method m = new Method(method_name, method_argcnt);
		return m;
	}
}