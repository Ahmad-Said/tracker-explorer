package application.datatype;

public class PositionImg {
	private double zoomLvl = 1, xLvl = 0.5, yLvl = 0.5;

	public PositionImg(double zoomLvl, double xLvl, double yLvl) {
		super();
		this.zoomLvl = zoomLvl;
		this.xLvl = xLvl;
		this.yLvl = yLvl;
	}

	public double getZoomLvl() {
		return zoomLvl;
	}

	public void setZoomLvl(double zoomLvl) {
		this.zoomLvl = zoomLvl;
	}

	public double getxLvl() {
		return xLvl;
	}

	public void setxLvl(double xLvl) {
		this.xLvl = xLvl;
	}

	public double getyLvl() {
		return yLvl;
	}

	public void setyLvl(double yLvl) {
		this.yLvl = yLvl;
	}
}
