package model.service.log;

import java.util.ArrayList;
import java.util.Stack;

public class AltLog extends MetaLog {
	private int id;
	private String type;
	private ArrayList<Stack<Log>> contents;

	public AltLog() {
		this.type = "alt";
		this.contents = new ArrayList<Stack<Log>>();
	}

	@Override
	public int getId() {
		return id;
	}
	@Override
	public String getType() {
		return type;
	}
	public Stack<Log> getChildArray(){
		// 一旦最初の要素を返す。
		return contents.get(1);
	}
	public void addContent(Stack<Log> content) {
		contents.add(content);
	}
}
