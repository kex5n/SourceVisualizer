package model.service;

import java.util.ArrayList;
import java.util.HashSet;

import model.domain.Attribute;
import model.domain.Class;
import model.domain.ExternalDependency;
import model.service.log.LogManager;
import model.service.log.Log;
import model.service.log.AltLog;

public class Player {
	private LogManager logManager;
	private int logArrayIndex;
	private boolean isRollbacking;
	private Log rollbackStartLog;

	public Player() {
		logArrayIndex = -1;
		isRollbacking = false;
		logManager = new LogManager();
	}
	public void recordMove(
		Class srcClass,
		Class dstClass,
		Attribute srcAttribute,
		HashSet<Attribute> relatedAttributes,
		HashSet<ExternalDependency> externalDependenies
	) {
		logManager.recordMoveLog(srcClass, dstClass, srcAttribute, relatedAttributes, externalDependenies);
		logArrayIndex++;
	}
	public String getLogText() {
		return logManager.getLogText();
	}
	public Log foward() {
		ArrayList<Log> currentLogArray = logManager.getCurrentUseLogArray();
		int nextIndex = logArrayIndex + 1;
		if (nextIndex >= currentLogArray.size()) {
			return null;
		}
		Log nextLog = currentLogArray.get(nextIndex);
		if (nextLog.getType().equals("alt")) {
			AltLog nextAltLog = (AltLog) nextLog;
			logManager.setCurrentUseLogArray(nextAltLog.getChildArray());
			logArrayIndex = 0;
			return logManager.getCurrentUseLogArray().get(logArrayIndex);
		} else {
			if (nextLog.equals(rollbackStartLog)) {
				isRollbacking = false;
				rollbackStartLog = null;
			}
			return nextLog;
		}
	}
	public ArrayList<Log> rollback() {
		isRollbacking = true;
		ArrayList<Log> currentUseLogArray = logManager.getCurrentUseLogArray();
		rollbackStartLog = currentUseLogArray.get(logArrayIndex);
		logArrayIndex--;
		if (logArrayIndex >= 0) {
			ArrayList<Log> returnLogArray = new ArrayList<Log>();
			for (int i=0; i < logArrayIndex + 1; i++) {
				returnLogArray.add(currentUseLogArray.get(i));
			}
			return returnLogArray;
		} else {
			return new ArrayList<Log>();
		}
	}
}
