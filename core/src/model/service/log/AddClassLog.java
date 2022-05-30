package model.service.log;

public class AddClassLog extends AddLog {
	private int id;
	private String name;
	private String type = "normal";
	private String actionType = "add";
	private String elementType = "class";

	public AddClassLog(String name) {
		this.name = name;
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
