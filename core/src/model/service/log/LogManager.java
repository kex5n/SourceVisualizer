package model.service.log;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import model.domain.Class;
import model.domain.Attribute;
import model.domain.ExternalDependency;


public class LogManager {
	private Stack<Log> wholeLogArray;
	private Stack<Log> currentUseLogArray;
	private Stack<Log> subLogArray;

	public LogManager() {
		wholeLogArray = new Stack<Log>();
		currentUseLogArray = wholeLogArray;
		subLogArray = new Stack<Log>();
	}

	public boolean subLogArrayIsEmpty() {
		return subLogArray.isEmpty();
	}
	public Log forward() {
		Log tempLog = subLogArray.pop();
		currentUseLogArray.push(tempLog);
		return tempLog;
	}
	public ArrayList<Log> rollback() {
		Log tempLog = currentUseLogArray.pop();
		subLogArray.push(tempLog);
		return getCurrentUseLogArray();
	}

	public void recordMoveLog(MoveLog moveLog) {
		if (subLogArray.isEmpty()) {
			currentUseLogArray.push(moveLog);
		} else {
			AltLog altLog = new AltLog();
			currentUseLogArray.push(altLog);
			currentUseLogArray = new Stack<Log>();
			currentUseLogArray.push(moveLog);
			altLog.addContent(currentUseLogArray);
			altLog.addContent(subLogArray);
			subLogArray = new Stack<Log>();
		}
	}

	public void recordAddLog(AddLog addLog) {
		currentUseLogArray.push(addLog);
	}

	public void recordAltLog(AltLog altLog) {
		currentUseLogArray.push(altLog);
	}

	public ArrayList<Log> getCurrentUseLogArray(){
		Stack<Log> tempCurrentUseLogArray = new Stack<Log>();
		ArrayList<Log> tempReturnArray = new ArrayList<Log>();
		while (!currentUseLogArray.isEmpty()) {
			Log tempLog = currentUseLogArray.pop();
			tempReturnArray.add(tempLog);
		}
		ArrayList<Log> returnArray = new ArrayList<Log>();
		for (int i=tempReturnArray.size() - 1; i>=0; i--) {
			returnArray.add(tempReturnArray.get(i));
			tempCurrentUseLogArray.push(tempReturnArray.get(i));
		}
		currentUseLogArray = tempCurrentUseLogArray;
		return returnArray;
	}

	public ArrayList<Log> getSubLogArray(){
		Stack<Log> tempSubLogArray = new Stack<Log>();
		ArrayList<Log> tempReturnArray = new ArrayList<Log>();
		while (!subLogArray.isEmpty()) {
			Log tempLog = subLogArray.pop();
			tempReturnArray.add(tempLog);
		}
		ArrayList<Log> returnArray = new ArrayList<Log>();
		for (int i=tempReturnArray.size() - 1; i>=0; i--) {
			returnArray.add(tempReturnArray.get(i));
			tempSubLogArray.push(tempReturnArray.get(i));
		}
		subLogArray = tempSubLogArray;
		return returnArray;
	}

	public void setCurrentUseLogArray(ArrayList<Log> currentUseLogArray){
		for (Log tempLog: currentUseLogArray) {
			this.currentUseLogArray.push(tempLog);
		}
	}

	public ArrayList<Log> getWholeLogArray(){
		Stack<Log> tempWholeLogArray = new Stack<Log>();
		ArrayList<Log> tempReturnArray = new ArrayList<Log>();
		while (!wholeLogArray.isEmpty()) {
			Log tempLog = wholeLogArray.pop();
			tempWholeLogArray.push(tempLog);
			tempReturnArray.add(tempLog);
		}
		ArrayList<Log> returnArray = new ArrayList<Log>();
		for (int i=tempReturnArray.size() - 1; i>=0; i--) {
			returnArray.add(tempReturnArray.get(i));
		}
		wholeLogArray = tempWholeLogArray;
		return returnArray;
	}

	public String generateMoveLogText(MoveLog moveLog) {
		String logText = "";
		logText += (
				"move: "
				+ moveLog.getName()
				+ ":"
				+ moveLog.getSrcClassName()
				+ " -> "
				+ moveLog.getDstClassName()
				+ "\n"
			);
			logText += "  automoved:\n";
			for (MoveLog autoMoveLog: moveLog.getAutoMoveArray()) {
				if (!autoMoveLog.getName().equals(moveLog.getName())) {
					logText += (
						"    "
						+ moveLog.getSrcClassName()
						+ "."
						+ autoMoveLog.getName()
						+ "\n"
					);
				}
			}
			logText += "  generated:\n";
			for (GeneratedLog generatedLog: moveLog.getGeneratedLogArray()) {
				logText += "    ";
				logText += generatedLog.getSrcClassName() + "." + generatedLog.getSrcAttributeName();
				logText += " -> ";
				logText += generatedLog.getDstClassName() + "." + generatedLog.getDstAttributeName();
				logText += "\n";
			}
			return logText;
	}

	public String generateAddLogText(AddLog addLog) {
		if (addLog.getElementType().equals("class")) {
			return "addC: " + addLog.getName() + "\n";
		} else if (addLog.getElementType().equals("method")) {
			AddMethodLog addMethodLog = (AddMethodLog) addLog;
			return "addM: " + addMethodLog.getElementType() + " to " + addMethodLog.getDstClassName() + "\n";
		} else {
			AddDependencyLog addDependencyLog = (AddDependencyLog) addLog;
			return (
				"addD: "
				+ addDependencyLog.getSrcClassName() + "." + addDependencyLog.getName()
				+ " to "
				+ addDependencyLog.getDstClassName() + "." + addDependencyLog.getDstAttributeName()
				+ "\n"
			);
		}
	}
	
	public String getLogText() {
		String logText = "";
		for (Log logElement: getCurrentUseLogArray()) {
			if (logElement.getType().equals("normal")) {
				NormalLog normalLogElement = (NormalLog) logElement;
				if (normalLogElement.getActionType().equals("move")) {
					MoveLog moveLogElement = (MoveLog) normalLogElement;
					logText += generateMoveLogText(moveLogElement);
				} else {
					AddLog addLogElement = (AddLog) normalLogElement;
					logText += generateAddLogText(addLogElement);
				}
			}
		}
		for (Log logElement: getSubLogArray()) {
			// logText += "subArray\n";
			if (logElement.getType().equals("normal")) {
				NormalLog normalLogElement = (NormalLog) logElement;
				if (normalLogElement.getActionType().equals("move")) {
					MoveLog moveLogElement = (MoveLog) normalLogElement;
					logText += generateMoveLogText(moveLogElement);
				} else {
					AddLog addLogElement = (AddLog) normalLogElement;
					logText += generateAddLogText(addLogElement);
				}
			}
		}
		return logText;
	}
}

