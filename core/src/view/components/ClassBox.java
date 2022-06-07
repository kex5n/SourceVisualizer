package view.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class ClassBox extends Box {
	private Point startPoint;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int CLASS_BOX_WIDTH = 600;
	public static final int CLASS_BOX_HEIGHT = 1000;
	private BitmapFont font = new BitmapFont();
	private FreeTypeFontGenerator fontGenerator;

	public ClassBox(Point startPoint, String name) {
		super(startPoint, CLASS_BOX_WIDTH, CLASS_BOX_HEIGHT, name);
		FileHandle file = Gdx.files.local("/home/kentaroishii/eclipse-workspace/sample/core/data/NuNimonade-M2.otf");
       fontGenerator = new FreeTypeFontGenerator(file);
       FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
       param.size = 40;
       param.color = Color.BLACK;
       this.font = fontGenerator.generateFont(param);
	}
	
	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(240/255f, 240/255f, 240/255f, 1.0f);
		shapeRenderer.rect(
				getX(),
				getY(),
				getWidth(),
				getHeight()
		);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(
				getX(),
				getY(),
				getWidth(),
				getHeight()
		);
		Point centerPoint = getCenterPoint();
		shapeRenderer.end();
		batch.begin();
		font.draw(
    		   batch,
    		   "p1." + getName(),
    		   centerPoint.x - 30,
    		   centerPoint.y + getHeight() / 2 - 25
    	);
		batch.end();
	}
}
