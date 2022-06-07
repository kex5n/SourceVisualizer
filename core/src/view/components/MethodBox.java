package view.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class MethodBox extends Box{
	private boolean isRemoved;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int METHOD_BOX_WIDTH = 400;
	public static final int METHOD_BOX_HEIGHT = 100;
	public BitmapFont font;
	private FreeTypeFontGenerator fontGenerator;

	public MethodBox(Point startPoint, String name, boolean isRemoved) {
		super(startPoint, METHOD_BOX_WIDTH, METHOD_BOX_HEIGHT, name);
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

	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		if (isRemoved) {
			shapeRenderer.setColor(128/255f, 10/255f, 74/255f, 1.0f);
		} else {
			shapeRenderer.setColor(1.0f, 20/255f, 147/255f, 1.0f);
		}
		shapeRenderer.rect(
				getX(),
				getY(),
				getWidth(),
				getHeight()
		);
		shapeRenderer.end();
		batch.begin();
		Point centerPoint = getCenterPoint();
       font.draw(
    		   batch,
    		   getName(),
    		   centerPoint.x - 20,
    		   centerPoint.y + 10
    	);
       batch.end();
	}
}
