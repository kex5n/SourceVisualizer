package view.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class PropertyBox extends Box {
	boolean isRemoved;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int PROPERTY_BOX_WIDTH = 300;
	public static final int PROPERTY_BOX_HEIGHT = 75;
	public BitmapFont font;
	private FreeTypeFontGenerator fontGenerator;

	public PropertyBox(Point startPoint, String name, boolean isRemoved) {
		super(startPoint, PROPERTY_BOX_WIDTH, PROPERTY_BOX_HEIGHT, name);
		this.isRemoved = isRemoved;
		FileHandle file = Gdx.files.local("/home/kentaroishii/eclipse-workspace/sample/core/data/NuNimonade-M2.otf");
       fontGenerator = new FreeTypeFontGenerator(file);
       FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
       param.size = 40;
       param.color = Color.BLACK;
       this.font = fontGenerator.generateFont(param);
	}

	public boolean getIsRemoved() {
		return isRemoved;
	}

	public void drawName(Batch batch) {
		Point centerPoint = getCenterPoint();
		font.draw(
    		   batch,
    		   getName(),
    		   centerPoint.x - 20,
    		   centerPoint.y + 10
    	);
	}

	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		if (isRemoved) {
			shapeRenderer.setColor(1.0f, 160/255f,  122/255f, 0.3f);
		} else {
			shapeRenderer.setColor(1.0f, 160/255f,  122/255f, 1.0f);
		}
		shapeRenderer.rect(
				getX(),
				getY(),
				getWidth(),
				getHeight()
		);
		shapeRenderer.end();
	}
}
