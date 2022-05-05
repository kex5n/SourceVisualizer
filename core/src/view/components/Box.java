package view.components;

import java.util.ArrayList;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Box extends Actor {
	private Point startPoint;

	public Box() {};
	public Box(Point startPoint, float width, float height) {
		this.startPoint = startPoint;
		this.setX(startPoint.x);
		this.setY(startPoint.y);
		this.setWidth(width);
		this.setHeight(height);
	}
	public Box(Point startPoint, float width, float height, String name) {
		this.startPoint = startPoint;
		this.setX(startPoint.x);
		this.setY(startPoint.y);
		this.setWidth(width);
		this.setHeight(height);
		this.setName(name);
	}
	public float getDefaultX() {
		return startPoint.x;
	}
	public float getDefaultY() {
		return startPoint.y;
	}
	public Point getLeftHeadPoint() {
		Point p = new Point(getX(), getY() + getHeight());
		return p;
	}
	public Point getLeftBottomPoint() {
		Point p = new Point(getX(), getY());
		return p;
	}
	public Point getRightHeadPoint() {
		Point p = new Point(getX() + getWidth(), getY() + getHeight());
		return p;
	}
	public Point getRightBottomPoint() {
		Point p = new Point(getX() + getWidth(), getY());
		return p;
	}
	public Point getCenterPoint() {
		Point p = new Point(getX() + getWidth() / 2, getY() + getHeight() / 2);
		return p;
	}
	public ArrayList<Point> getLeftConnectionPoints(int num){
		ArrayList<Point> connectionPoints = new ArrayList<Point>();
		float interval = getHeight() / (num + 1);
		for (int i = 0; i < num; i++) {
			Point p = new Point(getX(), getY() + interval * (i + 1));
			connectionPoints.add(p);
		}
		return connectionPoints;
	}
	public ArrayList<Point> getRightConnectionPoints(int num){
		ArrayList<Point> connectionPoints = new ArrayList<Point>();
		float interval = getHeight() / (num + 1);
		for (int i = 0; i < num; i++) {
			Point p = new Point(getX() + getWidth(), getY() + interval * (i + 1));
			connectionPoints.add(p);
		}
		return connectionPoints;
	}
}
