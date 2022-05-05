package model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;

import model.domain.Class;
import model.domain.Dependency;
import model.domain.Attribute;

public class DependencyResolver {
	public static HashMap<String, Object> resolve(Class c, String attributeName) {
		Attribute a = c.getAttribute(attributeName);
		ArrayList<Dependency> dependencyArray;

		HashSet<Attribute> visitedAttribute = new HashSet<Attribute>();
		HashSet<Dependency> visitedDependencies = new HashSet<Dependency>();
		Queue<Attribute> candidates = new ArrayDeque<Attribute>();
		candidates.add(a);
		while (candidates.size() > 0) {
			Attribute currentAttribute = candidates.poll();
			dependencyArray = c.getRelatedDependencies(currentAttribute);
			for (Dependency d: dependencyArray) {
				visitedDependencies.add(d);
				Attribute dst = d.getDst();
				if (!visitedAttribute.contains(dst)) {
					candidates.add(dst);
				}
			}
			visitedAttribute.add(currentAttribute);
		}
		HashMap<String, Object> returnMap = new HashMap<>();
		returnMap.put("attribute", visitedAttribute);
		returnMap.put("dependencies", visitedDependencies);
		return returnMap;
	}
}
