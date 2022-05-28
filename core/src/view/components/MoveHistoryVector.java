package view.components;

public class MoveHistoryVector {
	private Point startPoint;
	private Point endPoint;

	public MoveHistoryVector(Point startPoint, Point endPoint) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}
}
