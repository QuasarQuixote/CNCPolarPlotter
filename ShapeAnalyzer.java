package polarMaker;
import java.awt.*;
import java.awt.image.BufferedImage;

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
}
