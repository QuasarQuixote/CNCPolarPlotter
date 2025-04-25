package polarMaker;



public class CNCSpecs {
	public int rotationalSteps;
	public int displacingSteps;
	public int deadSteps;
	public int centerX;
	public int centerY;
	public double stepsPerPixel;
	
	CNCSpecs(int rotationalSteps, int displacingSteps, int deadSteps, int centerX, int centerY, double stepsPerPixel){
		this.rotationalSteps = rotationalSteps;
		this.displacingSteps = displacingSteps;
		this.deadSteps = deadSteps;
		this.centerX = centerX;
		this.centerY = centerY;
		this.stepsPerPixel = stepsPerPixel;
	}
}
