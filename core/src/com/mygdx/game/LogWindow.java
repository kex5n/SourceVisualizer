package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.Batch;

public class LogWindow extends ApplicationAdapter {
	String logText;
	float width;
	float height;
	private static SpriteBatch batch = new SpriteBatch();;
	private static BitmapFont font = new BitmapFont();

	public LogWindow(float width, float height) {
		logText = "";
		this.width = width;
		this.height = height;
		font.setColor(Color.BLACK);
		font.getData().setScale(6.0f);
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
