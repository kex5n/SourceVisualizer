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
	public static final int CLASS_BOX_WIDTH = 500;
	public static final int CLASS_BOX_HEIGHT = 1000;
	
	public ClassBox(Point startPoint, String name) {
		super(startPoint, CLASS_BOX_WIDTH, CLASS_BOX_HEIGHT, name);
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
				startPoint.x,
				startPoint.y,
				getWidth(),
				getHeight()
		);
		shapeRenderer.end();

		Point centerPoint = getCenterPoint();
		BitmapFont font = new BitmapFont();
       font.setColor(Color.BLACK);
       font.draw(
    		   batch,
    		   "p1." + getName(),
    		   centerPoint.x - 30,
    		   centerPoint.y + getHeight() / 2 - 25
    	);
	}
}
