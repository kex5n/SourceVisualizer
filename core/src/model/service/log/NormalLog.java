package model.service.log;

public abstract class NormalLog extends Log {
	public abstract String getName();
	public abstract String getActionType();
	public abstract String getElementType();
}
