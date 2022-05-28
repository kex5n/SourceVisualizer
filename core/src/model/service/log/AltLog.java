package model.service.log;

import java.util.ArrayList;

public class AltLog extends MetaLog {
	private int id;
	private String type;
	private ArrayList<ArrayList<Log>> contents;

	public AltLog(String type) {
		this.type = type;
	}

	@Override
	public int getId() {
		return id;
	}
	@Override
	public String getType() {
		return type;
	}
	public ArrayList<Log> getChildArray(){
		// 一旦最初の要素を返す。
		return contents.get(0);
	}
}
