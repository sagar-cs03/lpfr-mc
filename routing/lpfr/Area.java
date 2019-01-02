package routing.lpfr;

public class Area {
	private double area1, area2, area3, area4;
	private double totalArea;
	
	Area(double a1, double a2, double a3, double a4) {
		area1 = a1;
		area2 = a2;
		area3 = a3;
		area4 = a4;
		totalArea = area1 + area2 + area3 + area4;
	}
	
	Area() {
		area1 = area2 = area3 = area4  = -1;
		totalArea = -1;
	}

	public double getArea1() {
		return area1;
	}

	public void setArea1(double area1) {
		this.area1 = area1;
	}

	public double getArea2() {
		return area2;
	}

	public void setArea2(double area2) {
		this.area2 = area2;
	}

	public double getArea3() {
		return area3;
	}

	public void setArea3(double area3) {
		this.area3 = area3;
	}

	public double getArea4() {
		return area4;
	}

	public void setArea4(double area4) {
		this.area4 = area4;
	}

	public double getTotalArea() {
		return totalArea;
	}

	public void setTotalArea(double totalArea) {
		this.totalArea = totalArea;
	}
	
	
	
}