package model.service;

import java.util.ArrayList;
import java.util.HashSet;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import model.domain.Class;
import model.domain.Attribute;
import model.domain.ExternalDependency;

class LogHistory {
	private Class srcClass;
	private Class dstClass;
	private Attribute attribute;

	public LogHistory(Class srcClass, Class dstClass, Attribute attribute) {
		this.srcClass = srcClass;
		this.dstClass = dstClass;
		this.attribute = attribute;
	}

	public String getSrcClassName() {
		return srcClass.getName();
	}

	public String getDstClassName() {
		return dstClass.getName();
	}

	public String getAttributeName() {
		return attribute.getName();
	}
}

class GeneratedLogHistory {
	private Class srcClass;
	private Class dstClass;
	private Attribute srcAttribute;
	private Attribute dstAttribute;

	public GeneratedLogHistory(Class srcClass, Class dstClass, Attribute srcAttribute, Attribute dstAttribute) {
		this.srcClass = srcClass;
		this.dstClass = dstClass;
		this.srcAttribute = srcAttribute;
		this.dstAttribute = dstAttribute;
	}

	public String getSrcClassName() {
		return srcClass.getName();
	}

	public String getDstClassName() {
		return dstClass.getName();
	}

	public String getSrcAttributeName() {
		return srcAttribute.getName();
	}

	public String getDstAttributeName() {
		return dstAttribute.getName();
	}
}

class TransferLog{
	private LogHistory mainMove;
	private ArrayList<LogHistory> autoMoves;
	private ArrayList<GeneratedLogHistory> generatedMones;

	public TransferLog(LogHistory mainMove, ArrayList<LogHistory> autoMoves, ArrayList<GeneratedLogHistory> generatedMones) {
		this.mainMove = mainMove;
		this.autoMoves = autoMoves;
		this.generatedMones = generatedMones;
	}

	public LogHistory getMainMove() {
		return mainMove;
	}

	public ArrayList<LogHistory> getAutoMoves(){
		return autoMoves;
	}

	public ArrayList<GeneratedLogHistory> getGeneratedMones(){
		return generatedMones;
	}
}

public class LogManager {
	private ArrayList<TransferLog> transactionLogArray;
	
	public LogManager() {
		transactionLogArray = new ArrayList<TransferLog>();
	}

	public void recordTransactionLog(
			Class srcClass,
			Class dstClass,
			Attribute srcAttribute,
			HashSet<Attribute> relatedAttributes,
			HashSet<ExternalDependency> externalDependenies
	) {
		LogHistory mainMoveRecord = createMainMoveRecord(srcClass, dstClass, srcAttribute);
		ArrayList<LogHistory> autoMoveRecord = createAutoMoveRecord(srcClass, dstClass, relatedAttributes);
		ArrayList<GeneratedLogHistory> GeneratedRecord = createGeneratedRecord(srcClass, dstClass, externalDependenies);
		transactionLogArray.add(new TransferLog(mainMoveRecord, autoMoveRecord, GeneratedRecord));
	}

	public LogHistory createMainMoveRecord(Class srcClass, Class dstClass, Attribute attribute) {
		return new LogHistory(srcClass, dstClass, attribute);
	}

	public ArrayList<LogHistory> createAutoMoveRecord(Class srcClass, Class dstClass, HashSet<Attribute> attributes) {
		ArrayList<LogHistory> autoMoveRecords = new ArrayList<LogHistory>();
		for (Attribute a: attributes) {
			autoMoveRecords.add(new LogHistory(srcClass, dstClass, a));
		}
		return autoMoveRecords;
	}

	public ArrayList<GeneratedLogHistory> createGeneratedRecord(Class srcClass, Class dstClass, HashSet<ExternalDependency> externalDependenies) {
		ArrayList<GeneratedLogHistory> generatedRecords = new ArrayList<GeneratedLogHistory>();
		for (ExternalDependency d: externalDependenies){
			generatedRecords.add(
					new GeneratedLogHistory(srcClass, dstClass, srcClass.getAttribute(d.getSrcName()), dstClass.getAttribute(d.getDstName())
			));
		}
		return generatedRecords;
	}

	public String getLogText() {
		String logText = "";
		for (TransferLog tl: transactionLogArray) {
			LogHistory mainMove = tl.getMainMove();
			logText += "move: " + mainMove.getAttributeName() + ":" + mainMove.getSrcClassName() + " -> " + mainMove.getDstClassName() + "\n";
			logText += "  automoved:\n";
			for (LogHistory autoMoved: tl.getAutoMoves()) {
				if (!autoMoved.getAttributeName().equals(mainMove.getAttributeName())) {
					logText += "    " + mainMove.getSrcClassName() + "." + autoMoved.getAttributeName() + "\n";
				}
			}
			logText += "  generated:\n";
			for (GeneratedLogHistory generatedMoved: tl.getGeneratedMones()) {
				logText += "    ";
				logText += generatedMoved.getSrcClassName() + "." + generatedMoved.getSrcAttributeName();
				logText += " -> ";
				logText += generatedMoved.getDstClassName() + "." + generatedMoved.getDstAttributeName();
				logText += "\n";
			}
		}
		return logText;
	}
}
