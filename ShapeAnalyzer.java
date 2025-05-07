package polarMaker;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ShapeAnalyzer {

	private enum STEP_DIR {UP, UPRIGHT, RIGHT, DOWNRIGHT, DOWN, DOWNLEFT, LEFT, UPLEFT};
	private static final double ONE_OVER_ROOT_TWO = 0.70710678118;
	
	public static Polygon imageToPolygon(BufferedImage img) {
		// TODO Auto-generated method stub
		Polygon shape = new Polygon();
		findStart(img, shape);
		System.out.println("Start at: ("+shape.xpoints[0]+", "+shape.ypoints[0]+")");
		buildOutline(img, shape);
		return shape;
	}
	
	public static Polygon buildDrillPath(Polygon baseShape, int length) {
		Polygon drillPath = new Polygon();
		
		STEP_DIR fromStep;
		STEP_DIR toStep;
		Polygon relevantPoints = new Polygon();
		drillPath.addPoint(baseShape.xpoints[0], 0);
		
		relevantPoints = findRelevantPoints(0, baseShape, length);
		System.out.println("First point's relevant points are:  ");
		for(int i=0; i<relevantPoints.npoints; i++) System.out.print(relevantPoints.xpoints[i]+" "+relevantPoints.ypoints[i]+"  ");
		System.out.println();
		fromStep = findStep(baseShape.xpoints[0]-baseShape.xpoints[baseShape.npoints-2],baseShape.ypoints[0]-baseShape.ypoints[baseShape.npoints-2]);
		toStep = findStep(baseShape.xpoints[1]-baseShape.xpoints[0],baseShape.ypoints[1]-baseShape.ypoints[0]);
		System.out.println("Adding first point's drill path, from step:"+fromStep+" To Step: "+toStep);
		addToDrillPath(drillPath, relevantPoints, fromStep, toStep, baseShape.xpoints[0], baseShape.ypoints[0], length);
		for(int i=1; i<baseShape.npoints-1; i++) {
			fromStep=toStep;
			toStep = findStep(baseShape.xpoints[i+1]-baseShape.xpoints[i],baseShape.ypoints[i+1]-baseShape.ypoints[i]);
			relevantPoints = findRelevantPoints(i, baseShape, 2*length);
			addToDrillPath(drillPath, relevantPoints, fromStep, toStep, baseShape.xpoints[i], baseShape.ypoints[i], length);
		}
		return drillPath;
	}
	
	private static Polygon findRelevantPoints(int index, Polygon shape, int length) {
		Polygon relevantPoints = new Polygon();
		int x = shape.xpoints[index];
		int y = shape.ypoints[index];
		for(int i=0; i<shape.npoints; i++) {
			if(i!=index&&Math.abs(x-shape.xpoints[i])<length && Math.abs(y-shape.ypoints[i])<length) relevantPoints.addPoint(shape.xpoints[i], shape.ypoints[i]);
		}
		return relevantPoints;
	}
	
	private static void addToDrillPath(Polygon drillPath, Polygon relevantPoints, STEP_DIR fromStep, STEP_DIR toStep, int x, int y, int length) {
		System.out.print(fromStep+" to "+toStep+",");
		int toStepVal=toStep.ordinal();
		int fromStepVal=fromStep.ordinal();
		int stepCount=0;
		while(toStepVal!=fromStepVal) {
			stepCount++;
			fromStepVal++;
			if(fromStepVal>7)fromStepVal-=8;
		}
		if(stepCount>4)return;
		
		int checker=0;
		fromStep = step(fromStep, -4);
		STEP_DIR checkDir = step(fromStep, 2);
		while(checkDir!=step(toStep,-1)) {
			checker++;
			boolean valid = true;
			int drillX;
			int drillY;
			if(checkDir==STEP_DIR.UP) {
				drillX = x;
				drillY = y-length;
			} else if(checkDir==STEP_DIR.UPRIGHT) {
				drillX = (int)(x+length*ONE_OVER_ROOT_TWO);
				drillY =(int)(y-length*ONE_OVER_ROOT_TWO);
			}  else if(checkDir==STEP_DIR.RIGHT) {
				drillX = x+length;
				drillY =y;
			}  else if(checkDir==STEP_DIR.DOWNRIGHT) {
				drillX = (int)(x+length*ONE_OVER_ROOT_TWO);
				drillY =(int)(y+length*ONE_OVER_ROOT_TWO);
			}  else if(checkDir==STEP_DIR.DOWN) {
				drillX = x;
				drillY = y+length;
			}  else if(checkDir==STEP_DIR.DOWNLEFT) {
				drillX = (int)(x-length*ONE_OVER_ROOT_TWO);
				drillY =(int)(y+length*ONE_OVER_ROOT_TWO);
			}  else if(checkDir==STEP_DIR.LEFT) {
				drillX = x-length;
				drillY = y;
			} else {
				drillX = (int)(x-length*ONE_OVER_ROOT_TWO);
				drillY =(int)(y-length*ONE_OVER_ROOT_TWO);
			}
			checkDir = step(checkDir, 1);
			
			
			for(int j=0; j<relevantPoints.npoints; j++) {
				int deltaX = drillX - relevantPoints.xpoints[j];
				int deltaY = drillY - relevantPoints.ypoints[j];
				if (Math.ceil(Math.sqrt((deltaX*deltaX)+(deltaY*deltaY)))<length) {
					valid = false;
					System.out.println("Drill point ("+drillX+", "+drillY+") blocked by point # ("+relevantPoints.xpoints[j]+", "+relevantPoints.ypoints[j]+")");
				}
			}
			
			if(valid) {
				drillPath.addPoint(drillX, drillY);
				//System.out.println("Added Point ("+drillX+", "+drillY+")");
			}
		}
		System.out.println(" check point "+checker+" times");
	}
	
	private static void findStart(BufferedImage img, Polygon shape) {
		int WIDTH = img.getWidth();
		int HEIGHT = img.getHeight();
		int alpha;
		for(int y=0; y<HEIGHT; y++) {
			for(int x=0; x<WIDTH; x++) {
				alpha = img.getRGB(x, y);
				alpha = (alpha >> 24) & 0xFF;
				if(alpha>128) {
					shape.addPoint(x, y);
					return;
				}
			}
		}
		System.out.println("No valid point found.");
	}
	
	private static void buildOutline(BufferedImage img, Polygon shape) {
		int X_START = shape.xpoints[0];
		int Y_START = shape.ypoints[0];
		STEP_DIR lastStep = STEP_DIR.DOWN;
		STEP_DIR currentStep;
		int x = X_START;
		int y = Y_START;
		int counter = 1;
		int checkCounter=0;
		do {
			counter++;
			//System.out.println("Finding point #"+counter);
			currentStep = step(lastStep, 5);
			while(true) {
				//System.out.print("Checking "+currentStep.toString()+" ");
				if(check(x,y,currentStep, img)) break;
				currentStep = step(currentStep, 1);
				checkCounter++;
				if(checkCounter>8) {
					System.out.println("Error, this is weird");
					return;
				}
			}
			checkCounter=0;
			//System.out.println("\nDirection to point #"+counter+" is "+currentStep.toString());

			lastStep = currentStep;
			if(currentStep == STEP_DIR.UPRIGHT||currentStep == STEP_DIR.RIGHT||currentStep == STEP_DIR.DOWNRIGHT)
				x++;
			if(currentStep == STEP_DIR.UPLEFT||currentStep == STEP_DIR.LEFT||currentStep == STEP_DIR.DOWNLEFT)
				x--;
			if(currentStep == STEP_DIR.UPLEFT||currentStep == STEP_DIR.UP||currentStep == STEP_DIR.UPRIGHT)
				y--;
			if(currentStep == STEP_DIR.DOWNRIGHT||currentStep == STEP_DIR.DOWN||currentStep == STEP_DIR.DOWNLEFT)
				y++;
			shape.addPoint(x, y);
			//System.out.println("Added point: ("+x+", "+y+")");
		} while(x!=X_START || y!=Y_START);
		System.out.println("Done building the "+counter+" point outline!");
	}
	
	private static STEP_DIR step(STEP_DIR direction, int steps) {
		int startDir = direction.ordinal();
		int endDir = startDir + steps;
		return intToStep(endDir);
	}
	
	private static STEP_DIR intToStep(int stepNum) {
		while(stepNum<0) stepNum+=8;
		while(stepNum>=8) stepNum-=8;
		if(stepNum==0) return STEP_DIR.UP;
		if(stepNum==1) return STEP_DIR.UPRIGHT;
		if(stepNum==2) return STEP_DIR.RIGHT;
		if(stepNum==3) return STEP_DIR.DOWNRIGHT;
		if(stepNum==4) return STEP_DIR.DOWN;
		if(stepNum==5) return STEP_DIR.DOWNLEFT;
		if(stepNum==6) return STEP_DIR.LEFT;
		return STEP_DIR.UPLEFT;
	}
	
	private static boolean check(int x, int y, STEP_DIR dir, BufferedImage img) {
		if(dir == STEP_DIR.UP && y>0) if(((img.getRGB(x, y-1) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.UPRIGHT && y>0 && x<img.getWidth()-1) if(((img.getRGB(x+1, y-1) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.RIGHT && x<img.getWidth()-1) if(((img.getRGB(x+1, y) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.DOWNRIGHT && y<img.getHeight()-1 && x<img.getWidth()-1) if(((img.getRGB(x+1, y+1) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.DOWN && y<img.getHeight()-1) if(((img.getRGB(x, y+1) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.DOWNLEFT && y<img.getHeight()-1 && x>0) if(((img.getRGB(x-1, y+1) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.LEFT && x>0) if(((img.getRGB(x-1, y) >> 24) & 0xFF) > 128) return true;
		if(dir == STEP_DIR.UPLEFT && y>0 && x>0) if(((img.getRGB(x-1, y-1) >> 24) & 0xFF) > 128) return true;
		return false;
	}
	
	private static STEP_DIR findStep(int dx, int dy) {
		if(dx==0 && dy<0 )return STEP_DIR.UP;
		if(dx>0 && dy<0 )return STEP_DIR.UPRIGHT;
		if(dx>0 && dy==0 )return STEP_DIR.RIGHT;
		if(dx>0 && dy>0 )return STEP_DIR.DOWNRIGHT;
		if(dx==0 && dy>0 )return STEP_DIR.DOWN;
		if(dx<0 && dy>0 )return STEP_DIR.DOWNLEFT;
		if(dx<0 && dy==0 )return STEP_DIR.LEFT;
		if(dx<0 && dy<0) return STEP_DIR.UPLEFT;
		System.out.println("Invalid find step.");
		return STEP_DIR.UPLEFT;
	}
	
	public static ArrayList<Vector2> polygonToPolar(Polygon shape, CNCSpecs specs){
		ArrayList<Vector2> polarCords = new ArrayList<Vector2>();
		
		for(int i=0; i<shape.npoints; i++) {
			int x = shape.xpoints[i]-specs.centerX;
			int y = shape.ypoints[i]-specs.centerY;
			
			
			double r = Math.sqrt((x*x)+(y*y));
			double theta = Math.atan2(-1*y, x);
			if(theta<0)theta+=2*Math.PI;
			polarCords.add(new Vector2(r, theta));
			if(i==0) {
				System.out.println("x: "+x+", y: "+y+", r: "+r+", theta: "+theta);
			}
		}
		return polarCords;
	}
	
	public static ArrayList<Vector2> quantizeCords(ArrayList<Vector2> cords, CNCSpecs specs){
		ArrayList<Vector2> discreteCords = new ArrayList<Vector2>();
		int j=0;
		for(int i=0; i<cords.size(); i++) {
			
			double rSteps = (double)Math.round(cords.get(i).r * specs.stepsPerPixel);			
			double angleSteps = (double)Math.round((cords.get(i).theta*specs.rotationalSteps)/(2*Math.PI));
			
			if(j==0 || (rSteps!=discreteCords.getLast().r||angleSteps!=discreteCords.getLast().theta)) {
				discreteCords.add(new Vector2(rSteps, angleSteps));
				j++;
			}
		}
		
		return discreteCords;
	}
	
	public static ArrayList<Integer> uninterpolatedPoints(ArrayList<Vector2> discreteCords, CNCSpecs specs) {
		ArrayList<Integer> pointsThatNeedWork = new ArrayList<Integer>();
		
		for(int i=0; i<discreteCords.size()-1; i++) {
			int dr = (int)(discreteCords.get(i).r-discreteCords.get(i+1).r);
			int dTheta = (int)(discreteCords.get(i).theta-discreteCords.get(i+1).theta);
			if((Math.abs(Math.abs(dTheta)-specs.rotationalSteps)<=1)) System.out.println("X axis crossing at point: "+i);
			if((Math.abs(dTheta)>1 || Math.abs(dr)>1) && (Math.abs(Math.abs(dTheta)-specs.rotationalSteps)>1)) pointsThatNeedWork.add(i);
		}
		
		return pointsThatNeedWork;
	}
	
	public static ArrayList<Vector2> interpolate(ArrayList<Vector2> discreteCords, int index, CNCSpecs specs){
		//System.out.print("Interpolating point #"+index+"  ");
		
		ArrayList<Vector2> interpolatedPoints = new ArrayList<Vector2>();
		Vector2 start = discreteCords.get(index);
		Vector2 end = discreteCords.get(index+1);
		//System.out.print("Start: "+start+"  End: "+end+"   ");
		int dr = (int)(end.r-start.r);
		int dTheta = (int)(end.theta-start.theta);
		
		if(Math.abs(dTheta)>Math.abs(Math.abs(dTheta)-specs.rotationalSteps)) {
			System.out.print("Crossing stuff... index: "+index+", dtheta: "+dTheta);
			if(dTheta>0) {
				System.out.print(", dTheta is greater than 0");
				dTheta -= specs.rotationalSteps;
			}
			else if(dTheta<0) {
				System.out.print(", dTheta is greater than 0");
				dTheta += specs.rotationalSteps;
			}
			System.out.println(", new dtheta: "+dTheta);
		}
		//System.out.println(" dR: "+dr+",  dTheta: "+dTheta);
		if(dr == 0) {
			for(int i=1*(dTheta/Math.abs(dTheta)); i!=dTheta; i+=(dTheta/Math.abs(dTheta))) {
				interpolatedPoints.add(new Vector2(start.r, start.theta+i));
				if(interpolatedPoints.getLast().theta<0) interpolatedPoints.getLast().theta+=specs.rotationalSteps;
				if(interpolatedPoints.getLast().theta>specs.rotationalSteps) interpolatedPoints.getLast().theta-=specs.rotationalSteps;
			}
		} else if(dTheta ==0) {
			for(int i=1*(dr/Math.abs(dr)); i!=dr; i+= dr/Math.abs(dr)) {
				interpolatedPoints.add(new Vector2(start.r+i, start.theta));
			}
		} else if (Math.abs(dTheta)>Math.abs(dr)) {
			double drdTheta = (((double) dr)/dTheta);
			for(int i=1*(dTheta/Math.abs(dTheta)); i!=dTheta; i+= dTheta/Math.abs(dTheta)) {
				interpolatedPoints.add(new Vector2(start.r + Math.round(drdTheta*i), start.theta+i));
				if(interpolatedPoints.getLast().theta<0) interpolatedPoints.getLast().theta+=specs.rotationalSteps;
				if(interpolatedPoints.getLast().theta>specs.rotationalSteps) interpolatedPoints.getLast().theta-=specs.rotationalSteps;
			}
		} else {
			double dThetaDr = (((double) dTheta)/dr);
			for(int i=1*(dr/Math.abs(dr)); i!=dr; i+= dr/Math.abs(dr)) {
				interpolatedPoints.add(new Vector2(start.r + i, start.theta+Math.round(i*dThetaDr)));
				if(interpolatedPoints.getLast().theta<0) interpolatedPoints.getLast().theta+=specs.rotationalSteps;
				if(interpolatedPoints.getLast().theta>specs.rotationalSteps) interpolatedPoints.getLast().theta-=specs.rotationalSteps;
			}
		}
		
		
		return interpolatedPoints;
	}
	
	public static void addStart(ArrayList<Vector2> discreteCords, CNCSpecs specs) {
		discreteCords.add(0, new Vector2(specs.displacingSteps, discreteCords.get(0).theta));
	}
	
	public static void interpolatePath(ArrayList<Vector2> discretePath, ArrayList<Integer> indexes, CNCSpecs specs) {
		ArrayList<Vector2> subPath;
		int subPathLength;
		for(int i=0; i<indexes.size(); i++) {
			subPath = interpolate(discretePath, indexes.get(i), specs);
			subPathLength = subPath.size();
			for(int j=0; j<subPathLength; j++) {
				discretePath.add((indexes.get(i)+1+j), subPath.get(j));
			}
			for(int j=i+1; j<indexes.size(); j++) {
				indexes.set(j, indexes.get(j)+subPathLength);
			}
		}
	}
	public static void writePath(FileWriter outputWriter, ArrayList<Vector2> cncCords, CNCSpecs specs) throws IOException {
		Vector2 start = cncCords.get(0);
		Vector2 end = cncCords.get(1);
		int dr;
		int dtheta;
		for(int i=0; i<cncCords.size()-1; i++) {
			end = cncCords.get(i+1);
			dr = (int)(end.r-start.r);
			dtheta = (int)(end.theta-start.theta);
			if(dtheta>specs.rotationalSteps/2) {
				System.out.print("wrappping dTheta start: "+dtheta);
				dtheta -= specs.rotationalSteps;
				System.out.print(" end: "+dtheta);
			}
			if(dtheta<-1*specs.rotationalSteps/2) {
				System.out.print("wrappping dTheta start: "+dtheta);
				dtheta += specs.rotationalSteps;
				System.out.print(" end: "+dtheta);
			}
			if(dr==1&&dtheta==0) outputWriter.write("0");
			else if(dr==1&&dtheta==1) outputWriter.write("1");
			else if(dr==0&&dtheta==1) outputWriter.write("2");
			else if(dr==-1&&dtheta==1) outputWriter.write("3");
			else if(dr==-1&&dtheta==0) outputWriter.write("4");
			else if(dr==-1&&dtheta==-1) outputWriter.write("5");
			else if(dr==0&&dtheta==-1) outputWriter.write("6");
			else if(dr==1&&dtheta==-1) outputWriter.write("7");
			else System.out.println("Disctontinuity at point: "+i+", dr = "+dr+", dtheta = "+dtheta);
			start = end;
		}
	}
	
	public static Polygon cncCordsToPolygon(ArrayList<Vector2> cncCords, CNCSpecs specs) {
		Polygon cncRepresentation = new Polygon();
		
		int x;
		int y;
		int j = 0;
		for(int i=0; i<cncCords.size(); i++) {
			x = (int)(specs.centerX + (cncCords.get(i).r/specs.stepsPerPixel)*( Math.cos(2*Math.PI*((cncCords.get(i).theta)/(specs.rotationalSteps)))));
			y = (int)(specs.centerY - (cncCords.get(i).r/specs.stepsPerPixel)*( Math.sin(2*Math.PI*((cncCords.get(i).theta)/(specs.rotationalSteps)))));
			if(j==0||x!=cncRepresentation.xpoints[j-1]||y!=cncRepresentation.xpoints[j-1]) {
				cncRepresentation.addPoint(x, y);
				j++;
			}
		}
		
		return cncRepresentation;
	}
}
