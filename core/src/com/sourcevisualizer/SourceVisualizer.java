package com.sourcevisualizer;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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


public class SourceVisualizer extends ApplicationAdapter {
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

		p = PackageFactory.create(); 
		drawer = new Drawer(p, stage);

		app = (Lwjgl3Application) Gdx.app;
	
		// create log window
		Lwjgl3WindowConfiguration logConfig = new Lwjgl3WindowConfiguration();
		DisplayMode logMode = Gdx.graphics.getDisplayMode();
		logConfig.setWindowPosition(logMode.width - 640, logMode.height);
		logConfig.setTitle("Log");
		logConfig.setResizable(false);
		ApplicationListener logListener = new LogWindow(logMode.width, logMode.height);
		logWindow = app.newWindow(logListener, logConfig);

		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		String logText = drawer.getLogText();
		LogWindow tempLogWindow = (LogWindow) logWindow.getListener();
		tempLogWindow.setLogText(logText);
		stage.act(Gdx.graphics.getDeltaTime());
		
		sharedSpriteBatch.begin();
		//stage.draw();
		sharedSpriteBatch.end();
		drawer.draw(shapeRenderer, sharedSpriteBatch);
	}
	
	@Override
	public void dispose () {
		sharedSpriteBatch.dispose();
		logWindow.closeWindow();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
