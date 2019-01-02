package routing.lpfr;

import core.Coord;
import core.DTNHost;

public class NeighbourInfo {
	private double angleBeta;
	private int state;
	private DTNHost neighbour;
	private Coord tranCord;
	private Area area;
	
	NeighbourInfo(double angle, int state, DTNHost neighbour, Coord tranCord) {
		this.angleBeta = angle;
		this.state = state;
		this.neighbour = neighbour;
		this.tranCord = tranCord;
		this.area = null; 
	}
	
	NeighbourInfo() {
		this.angleBeta = -360;
		this.state = -1;
		this.neighbour = null;
		this.area = null ;
		this.tranCord = null; 
	}

	public double getAngleBeta() {
		return angleBeta;
	}

	public void setAngleBeta(double angleBeta) {
		this.angleBeta = angleBeta;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public DTNHost getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(DTNHost neighbour) {
		this.neighbour = neighbour;
	}

	public Coord getTranCord() {
		return tranCord;
	}

	public void setTranCord(Coord tranCord) {
		this.tranCord = tranCord;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}
	
	
	
}
