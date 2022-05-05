package view.components;

public class DependencyVector {
	private Point startPoint;
	private Point endPoint;
	private int distance;
	private boolean isMethodDst;

	public DependencyVector(Point startPoint, Point endPoint, int distance, boolean isMethodDst) {
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.distance = distance;
		this.isMethodDst = isMethodDst;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public int getDistance() {
		return distance;
	}

	public boolean getIsMethodDst() {
		return isMethodDst;
	}
}
