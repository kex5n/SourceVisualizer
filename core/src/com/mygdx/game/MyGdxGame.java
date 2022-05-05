package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import model.domain.Package;
import model.service.PackageFactory;

import view.Drawer;

public class MyGdxGame extends ApplicationAdapter {
	static SpriteBatch sharedSpriteBatch;
	static Lwjgl3Application app;
	static Lwjgl3Window logWindow;
	static ShapeRenderer shapeRenderer;
	static Stage stage;

	Package p;
	Drawer drawer;

	@Override
	public void create () {
		sharedSpriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		p = PackageFactory.create(); 
		drawer = new Drawer(p, stage);

		app = (Lwjgl3Application) Gdx.app;
		Lwjgl3WindowConfiguration config = new Lwjgl3WindowConfiguration();
		DisplayMode mode = Gdx.graphics.getDisplayMode();
		config.setWindowPosition(mode.width - 640, mode.height);
		config.setTitle("Log");
		config.setResizable(false);
		ApplicationListener listener = new LogWindow();
		logWindow = app.newWindow(listener, config);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// drawer.draw(shapeRenderer, sharedSpriteBatch);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		drawer.draw(shapeRenderer, sharedSpriteBatch);
	}
	
	@Override
	public void dispose () {
		sharedSpriteBatch.dispose();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
}
