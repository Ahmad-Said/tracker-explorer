package said.ahmad.javafx.tracker.datatype;

public class ImagePosition implements Cloneable {
	private double zoomLvl, xLvl, yLvl;

	public ImagePosition(double zoomLvl, double xLvl, double yLvl) {
		super();
		this.zoomLvl = zoomLvl;
		this.xLvl = xLvl;
		this.yLvl = yLvl;
	}

	public ImagePosition() {
		zoomLvl = 1;
		xLvl = 0.5;
		yLvl = 0.5;
	}

	public double getZoomLvl() {
		return zoomLvl;
	}

	public ImagePosition setZoomLvl(double zoomLvl) {
		this.zoomLvl = zoomLvl;
		return this;
	}

	public double getxLvl() {
		return xLvl;
	}

	public ImagePosition setxLvl(double xLvl) {
		this.xLvl = xLvl;
		return this;
	}

	public double getyLvl() {
		return yLvl;
	}

	public ImagePosition setyLvl(double yLvl) {
		this.yLvl = yLvl;
		return this;
	}

	@Override
	public String toString() {
		return "PositionImg [zoomLvl=" + zoomLvl + ", xLvl=" + xLvl + ", yLvl=" + yLvl + "]";
	}

	@Override
	public ImagePosition clone() {
		return new ImagePosition(zoomLvl, xLvl, yLvl);
	}
}
