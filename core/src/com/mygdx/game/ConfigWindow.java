package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ConfigWindow extends ApplicationAdapter {
	float width;
	float height;
	private static SpriteBatch batch = new SpriteBatch();;
	private static BitmapFont font = new BitmapFont();
	private TextButton forwardButton;
	public Boolean isClicked = false;
	public Stage stage;

	public Boolean getIsClicked() {
		return isClicked;
	}
	
	public ConfigWindow(float width, float height) {
		this.width = width;
		this.height = height;
		this.stage = new Stage();
		initButton();
		this.stage.addActor(forwardButton);
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

		// stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	private void initButton() {
		Skin skin = new Skin();

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));

		// Store the default libGDX font under the name "default".
		skin.add("default", new BitmapFont());

		// Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.font.getData().setScale(10.0f);
		skin.add("default", textButtonStyle);
		forwardButton = new TextButton("forward", skin);
		forwardButton.setSize(700, 150);
		forwardButton.setPosition(10, 10);

		forwardButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				System.out.println("Clicked!");
            }
		});
	}
}
