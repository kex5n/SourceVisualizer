package view.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class MethodBox extends Box{
	private boolean isRemoved;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static final int METHOD_BOX_WIDTH = 400;
	public static final int METHOD_BOX_HEIGHT = 100;
	public static final BitmapFont font = new BitmapFont();

	public MethodBox(Point startPoint, String name, boolean isRemoved) {
		super(startPoint, METHOD_BOX_WIDTH, METHOD_BOX_HEIGHT, name);
		this.isRemoved = isRemoved;
		font.setColor(Color.BLACK);
		font.getData().setScale(2.0f);
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
			shapeRenderer.setColor(Color.DARK_GRAY);
		} else {
			shapeRenderer.setColor(Color.YELLOW);
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
