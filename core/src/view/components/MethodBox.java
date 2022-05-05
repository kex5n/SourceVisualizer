package view.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class MethodBox extends Box{
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int METHOD_BOX_WIDTH = 400;
	public static final int METHOD_BOX_HEIGHT = 100;
	
	public MethodBox(Point startPoint, String name) {
		super(startPoint, METHOD_BOX_WIDTH, METHOD_BOX_HEIGHT, name);
	}

	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.YELLOW);
		shapeRenderer.rect(
				getX(),
				getY(),
				getWidth(),
				getHeight()
		);
		shapeRenderer.end();
  
		BitmapFont font = new BitmapFont();
       font.setColor(Color.BLACK);
       Point centerPoint = getCenterPoint();
       font.draw(
    		   batch,
    		   getName(),
    		   centerPoint.x - 20,
    		   centerPoint.y + 10
    	);
	}
}
