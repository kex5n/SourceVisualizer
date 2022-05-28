package model.service.log;

import java.util.ArrayList;
import java.util.HashSet;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import model.domain.Class;
import model.domain.Attribute;
import model.domain.ExternalDependency;


public class LogManager {
	private ArrayList<Log> wholeLogArray;
	private ArrayList<Log> currentUseLogArray;
	
	public LogManager() {
		wholeLogArray = new ArrayList<Log>();
		currentUseLogArray = wholeLogArray;
	}

	public void recordMoveLog(
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
		currentUseLogArray.add(moveLog);
	}

	public ArrayList<MoveLog> createAutoMoveLogs(Class srcClass, Class dstClass, HashSet<Attribute> attributes) {
		ArrayList<MoveLog> autoMoveLogs = new ArrayList<MoveLog>();
		for (Attribute a: attributes) {
			autoMoveLogs.add(new MoveLog(a.getName(), srcClass.getName(), dstClass.getName()));
		}
		return autoMoveLogs;
	}

	public ArrayList<GeneratedLog> createGeneratedLogs(Class srcClass, Class dstClass, HashSet<ExternalDependency> externalDependenies) {
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

	public ArrayList<Log> getCurrentUseLogArray(){
		return currentUseLogArray;
	}
	public void setCurrentUseLogArray(ArrayList<Log> currentUseLogArray){
		this.currentUseLogArray = currentUseLogArray;
	}

	public ArrayList<Log> getWholeLogArray(){
		return wholeLogArray;
	}

	public String getLogText() {
		String logText = "";
		for (Log logElement: wholeLogArray) {
			if (logElement.getType().equals("normal")) {
				NormalLog normalLogElement = (NormalLog) logElement;
				if (normalLogElement.getActionType().equals("move")) {
					MoveLog moveLogElement = (MoveLog) normalLogElement;
					logText += (
						"move: "
						+ moveLogElement.getName()
						+ ":"
						+ moveLogElement.getSrcClassName()
						+ " -> "
						+ moveLogElement.getDstClassName()
						+ "\n"
					);
					logText += "  automoved:\n";
					for (MoveLog autoMoveLog: moveLogElement.getAutoMoveArray()) {
						if (!autoMoveLog.getName().equals(moveLogElement.getName())) {
							logText += (
								"    "
								+ moveLogElement.getSrcClassName()
								+ "."
								+ moveLogElement.getName()
								+ "\n"
							);
						}
					}
					logText += "  generated:\n";
					for (GeneratedLog generatedLog: moveLogElement.getGeneratedLogArray()) {
						logText += "    ";
						logText += generatedLog.getSrcClassName() + "." + generatedLog.getSrcAttributeName();
						logText += " -> ";
						logText += generatedLog.getDstClassName() + "." + generatedLog.getDstAttributeName();
						logText += "\n";
					}
				}
			}
			
			
		}
		return logText;
	}
}

