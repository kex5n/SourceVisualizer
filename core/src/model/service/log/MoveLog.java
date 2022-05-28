package model.service.log;

import java.util.ArrayList;

public class MoveLog extends NormalLog {
	private int id;
	private String name;
	private String type = "normal";
	private String actionType = "move";
	private String elementType = "method";
	private String srcClassName;
	private String dstClassName;
	private ArrayList<MoveLog> autoMoveArray;
	private ArrayList<GeneratedLog> generatedLogArray;

	public MoveLog(String name, String srcClassName, String dstClassName) {
		this.name = name;
		this.elementType = elementType;
		this.srcClassName = srcClassName;
		this.dstClassName = dstClassName;
	}

	public void setAutoMoveArray(ArrayList<MoveLog> autoMoveArray) {
		this.autoMoveArray = autoMoveArray;
	}

	public ArrayList<MoveLog> getAutoMoveArray(){
		return autoMoveArray;
	}

	public void setGeneratedLogArray(ArrayList<GeneratedLog> generatedLogArray) {
		this.generatedLogArray = generatedLogArray;
	}

	public ArrayList<GeneratedLog> getGeneratedLogArray(){
		return generatedLogArray;
	}

	@Override
	public int getId() {
		return id;
	};
	@Override
	public String getName() {
		return name;
	};
	@Override
	public String getType() {
		return type;
	};
	@Override
	public String getActionType() {
		return actionType;
	};
	@Override
	public String getElementType() {
		return elementType;
	};

	public String getSrcClassName() {
		return srcClassName;
	};
	public String getDstClassName() {
		return dstClassName;
	};

	public int hashCode() {
		return (
			id
			+ name.hashCode()
			+ type.hashCode()
			+ actionType.hashCode()
			+ elementType.hashCode()
			+ srcClassName.hashCode()
			+ dstClassName.hashCode()
		);
	}
}
