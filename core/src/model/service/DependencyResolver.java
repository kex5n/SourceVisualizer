package model.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;

import model.domain.Class;
import model.domain.InternalDependency;
import model.domain.ExternalDependency;
import model.domain.Attribute;
import model.service.log.GeneratedLog;
import model.service.log.MoveLog;

public class DependencyResolver {
	public static MoveLog resolve(Class originalSrcClass, Class originalDstClass, String srcAttributeName) {
		Class srcClass = originalSrcClass.clone();
		Class dstClass = originalDstClass.clone();
	
		Attribute a = srcClass.getAttribute(srcAttributeName);
		ArrayList<InternalDependency> srcInternalDependencyArray;
		HashSet<InternalDependency> visitedSrcInternalDependencySet;
		ArrayList<InternalDependency> dstInternalDependencyArray;
		ArrayList<ExternalDependency> externalDependencyArray;

		// 1. 内部依存関係のうち、移動するAttributeがsrcであるとき、dstも合わせて移す。
		HashSet<Attribute> moveAttribute = new HashSet<Attribute>();  // 移動するAttribute
		HashSet<InternalDependency> moveInternalDependency = new HashSet<InternalDependency>();  // 移動する内部依存関係
		HashSet<String> visitedAttributeName = new HashSet<String>();
		Queue<Attribute> candidates = new ArrayDeque<Attribute>();  // 依存関係を調べるAttributeの候補
		candidates.add(a);
		while (candidates.size() > 0) {  // 候補がなくなり、探索が完了するまで繰り返す
			Attribute currentAttribute = candidates.poll();
			moveAttribute.add(currentAttribute);
			visitedAttributeName.add(a.getName());
			srcInternalDependencyArray = srcClass.getSrcInternalDependencies(currentAttribute);
			moveInternalDependency.addAll(srcInternalDependencyArray);
			for (InternalDependency d: srcInternalDependencyArray) {
				String dstName = d.getDstName();
				if (!visitedAttributeName.contains(dstName)) {
					candidates.add(srcClass.getAttribute(dstName));
				}
			}
			moveAttribute.add(currentAttribute);
		}
		// Attributeを移す
		for (Attribute moveA: moveAttribute) {
			srcClass.removeAttribute(moveA.getName());
			dstClass.setAttribute(moveA);
		}
		// 内部依存関係を移す
		for (InternalDependency d: moveInternalDependency) {
			srcClass.removeInternalDependency(d);
			try {
				dstClass.setInternalDependencies(d.getSrcName(), d.getDstName());
			} catch(Exception e) {
				System.exit(1);
			}
		}

		// 2. 元のクラスのうち、外部依存関係だったものが移動により内部依存関係になった場合、外部依存関係を削除し、内部依存関係に追加する。
		ArrayList<ExternalDependency> srcCurrentExternalDependencies = (ArrayList<ExternalDependency>) srcClass.getExternalDependencies().clone();
		for (ExternalDependency externalDependency: srcCurrentExternalDependencies) {
			if (srcClass.has(externalDependency.getSrcName()) & srcClass.has(externalDependency.getDstName())) {
				srcClass.removeExternalDependency(externalDependency);
				try {
					srcClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}

		// 3. 移動先のクラスのうち、外部依存関係だったものが移動により内部依存関係になった場合、外部依存関係を削除し、内部依存関係に追加する。
		ArrayList<ExternalDependency> dstCurrentExternalDependencies = (ArrayList<ExternalDependency>) dstClass.getExternalDependencies().clone();
		for (ExternalDependency externalDependency: dstCurrentExternalDependencies) {
			if (dstClass.has(externalDependency.getSrcName()) & dstClass.has(externalDependency.getDstName())) {
				dstClass.removeExternalDependency(externalDependency);
				try {
					dstClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}

		// 4. 移ったAttributeのうち、元のクラスに残ったAttributeに依存されている場合、外部依存関係とする。
		HashSet<ExternalDependency> externalDependencies = new HashSet<ExternalDependency>();
		ArrayList<InternalDependency> srcCurrentInternalDependencies = (ArrayList<InternalDependency>) srcClass.getInternalDependencies().clone();
		for (InternalDependency internalDependency: srcCurrentInternalDependencies) {
			if (internalDependency.getDstName().equals(a.getName())) {
				srcClass.removeInternalDependency(internalDependency);
				try {
					srcClass.setExternalDependencies(internalDependency.getSrcName(), dstClass, internalDependency.getDstName());
					externalDependencies.add(new ExternalDependency(internalDependency.getSrcName(), dstClass.getName(), internalDependency.getDstName()));
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}
		return createMoveLog(
			srcClass, dstClass, a, moveAttribute, externalDependencies
		);
	}

	private static MoveLog createMoveLog(
			Class srcClass,
			Class dstClass,
			Attribute srcAttribute,
			HashSet<Attribute> relatedAttributes,
			HashSet<ExternalDependency> externalDependenies
	) {
		MoveLog moveLog = new MoveLog(
			srcAttribute.getName(), srcClass.getName(), dstClass.getName()
		);
		ArrayList<MoveLog> autoMoveLogs = createAutoMoveLogs(srcClass, dstClass, relatedAttributes);
		moveLog.setAutoMoveArray(autoMoveLogs);

		ArrayList<GeneratedLog> Generatedlogs = createGeneratedLogs(srcClass, dstClass, externalDependenies);
		moveLog.setGeneratedLogArray(Generatedlogs);
		return moveLog;
		
	}

	private static ArrayList<MoveLog> createAutoMoveLogs(Class srcClass, Class dstClass, HashSet<Attribute> attributes) {
		ArrayList<MoveLog> autoMoveLogs = new ArrayList<MoveLog>();
		for (Attribute a: attributes) {
			autoMoveLogs.add(new MoveLog(a.getName(), srcClass.getName(), dstClass.getName()));
		}
		return autoMoveLogs;
	}

	private static ArrayList<GeneratedLog> createGeneratedLogs(Class srcClass, Class dstClass, HashSet<ExternalDependency> externalDependenies) {
		ArrayList<GeneratedLog> generatedLogs = new ArrayList<GeneratedLog>();
		for (ExternalDependency d: externalDependenies){
			generatedLogs.add(
				new GeneratedLog(
					srcClass.getName(),
					dstClass.getName(),
					d.getSrcName(), 
					d.getDstName()
				)
			);
		}
		return generatedLogs;
	}
}
