package view.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ClassBox extends Box {
	private Point startPoint;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int CLASS_BOX_WIDTH = 600;
	public static final int CLASS_BOX_HEIGHT = 1000;
	public static final BitmapFont font = new BitmapFont();

	public ClassBox(Point startPoint, String name) {
		super(startPoint, CLASS_BOX_WIDTH, CLASS_BOX_HEIGHT, name);
		font.setColor(Color.BLACK);
		font.getData().setScale(2.0f);
	}

	public void drawName(Batch batch) {
		Point centerPoint = getCenterPoint();
		font.draw(
    		   batch,
    		   "p1." + getName(),
    		   centerPoint.x - 30,
    		   centerPoint.y + getHeight() / 2 - 25
    	);
	}
	
	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(240/255f, 240/255f, 240/255f, 0.5f);
		Point startPoint = getLeftBottomPoint();
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
		shapeRenderer.end();
	}
}
