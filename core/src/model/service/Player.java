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
	private Log rollbackStartLog;

	public Player() {
		logManager = new LogManager();
	}

	public void recordMoveLog(MoveLog moveLog) {
		if (logManager.subLogArrayIsEmpty()) {
			logManager.recordMoveLog(moveLog);
		} else {
			System.out.println("is no't empty!");
		}
	}
	public String getLogText() {
		return logManager.getLogText();
	}
	public Log foward() {
		Log nextLog = logManager.forward();
		if (nextLog.getType().equals("alt")) {
			System.out.println("Not implement yet.");
			return nextLog;
		} else {
			if (nextLog.equals(rollbackStartLog)) {
				rollbackStartLog = null;
			}
			return nextLog;
		}
	}
	public ArrayList<Log> rollback() {
		return logManager.rollback();
	}
	public ArrayList<Log> getCurrentValidLogArray(){
		return logManager.getCurrentUseLogArray();
	}
}
