package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class ConfigWindow extends ApplicationAdapter {
	float width;
	float height;
	private static SpriteBatch batch = new SpriteBatch();;
	private static BitmapFont font = new BitmapFont();

	public ConfigWindow(float width, float height) {
		this.width = width;
		this.height = height;
		font.setColor(Color.BLACK);
		font.getData().setScale(6.0f);
	}

	@Override
	public void create () {
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		this.batch.begin();
		this.batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
