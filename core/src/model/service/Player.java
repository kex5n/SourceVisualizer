package model.service;

import java.util.ArrayList;
import java.util.HashSet;

import model.domain.Attribute;
import model.domain.Class;
import model.domain.ExternalDependency;
import model.service.log.LogManager;
import model.service.log.MoveLog;
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

	public void recordMoveLog(MoveLog moveLog) {
		logManager.recordMoveLog(moveLog);
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
			logArrayIndex = nextIndex;
			return nextLog;
		}
	}
	public ArrayList<Log> rollback() {
		isRollbacking = true;
		ArrayList<Log> currentUseLogArray = logManager.getCurrentUseLogArray();
		rollbackStartLog = currentUseLogArray.get(logArrayIndex);
		logArrayIndex--;
		if (logArrayIndex >= 0) {
			return getCurrentValidLogArray();
		} else {
			return new ArrayList<Log>();
		}
	}
	public ArrayList<Log> getCurrentValidLogArray(){
		ArrayList<Log> returnLogArray = new ArrayList<Log>();
		for (int i=0; i < logArrayIndex + 1; i++) {
			returnLogArray.add(logManager.getCurrentUseLogArray().get(i));
		}
		return returnLogArray;
	}
}
