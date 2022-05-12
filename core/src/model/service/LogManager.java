package model.service;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import model.domain.Class;

public class LogManager {
	private String logText;

	public LogManager() {
		logText = "";
	}

	public void recordMainMove(String srcName, Class srcClass, Class dstClass) {
		logText += "move: " + srcName + ":" + srcClass.getName() + " -> " + dstClass.getName() + "\n";
	}
	public String getLogText() {
		return logText;
	}
}
