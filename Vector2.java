package polarMaker;

public class Vector2 {
	public double r;
	public double theta;
	
	Vector2(double r, double theta){
		this.r = r;
		this.theta = theta;
	}
	
	public String toString() {
		return "(r: "+r+", theta: "+theta+")";
	}
}
