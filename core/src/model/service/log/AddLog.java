package model.service.log;

public class AddLog extends NormalLog {
	private int id;
	private String name;
	private String type = "normal";
	private String actionType = "add";
	private String elementType;
	private String dstClassName;

	public AddLog(String elementType, String dstClassName) {
		this.elementType = elementType;
		this.dstClassName = dstClassName;
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
	@Override
	public String getDstClassName() {
		return dstClassName;
	}

	public int hashCode() {
		return (
			id
			+ name.hashCode()
			+ type.hashCode()
			+ actionType.hashCode()
			+ elementType.hashCode()
		);
	}
}
