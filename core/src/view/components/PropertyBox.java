package view.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class PropertyBox extends Box {
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int PROPERTY_BOX_WIDTH = 300;
	public static final int PROPERTY_BOX_HEIGHT = 75;
	
	public PropertyBox(Point startPoint, String name) {
		super(startPoint, PROPERTY_BOX_WIDTH, PROPERTY_BOX_HEIGHT, name);
	}

	@Override
	public void draw(Batch batch, float alpha) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.ORANGE);
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
