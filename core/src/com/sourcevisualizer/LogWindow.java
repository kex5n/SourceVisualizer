package com.sourcevisualizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class LogWindow extends ApplicationAdapter {
	String logText;
	float width;
	float height;
	private static SpriteBatch batch = new SpriteBatch();;
	public BitmapFont font;
	private FreeTypeFontGenerator fontGenerator;

	public LogWindow(float width, float height) {
		logText = "";
		this.width = width;
		this.height = height;
		FileHandle file = Gdx.files.local("path/to/otffile");
       fontGenerator = new FreeTypeFontGenerator(file);
       FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
       param.size = 60;
       param.color = Color.BLACK;
       this.font = fontGenerator.generateFont(param);
	}
	
	public void setLogText(String logText) {
		this.logText = logText;
	}
	
	@Override
	public void create () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.batch.begin();
		font.draw(batch, logText, 100, 1100);
		this.batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
