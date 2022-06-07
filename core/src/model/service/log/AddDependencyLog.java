package model.service.log;

public class AddDependencyLog extends AddLog {
	private int id;
	private String name;
	private String type = "normal";
	private String actionType = "add";
	private String elementType = "dependency";
	private String srcClassName;
	private String dstClassName;
	private String dstAttributeName;

	public AddDependencyLog(
		String srcClassName,
		String srcMethodName,
		String dstClassName,
		String dstAttributeName
	) {
		this.srcClassName = srcClassName;
		this.name = srcMethodName;
		this.dstClassName = dstClassName;
		this.dstAttributeName = dstAttributeName;
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
	}
	public String getDstClassName() {
		return dstClassName;
	}
	public String getDstAttributeName() {
		return dstAttributeName;
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
