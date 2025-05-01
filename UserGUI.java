package polarMaker;
import java.awt.FlowLayout;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.*;

public class UserGUI {
	public static void main(String[] args) throws IOException {
		JFrame gui = new JFrame("Picture to Polar Converter");
		gui.setSize(2000,2000);
		gui.getContentPane().setLayout(new FlowLayout());
		//Options:
		//H:\\real_eclipse\\CS3\\src\\polarMaker\\Drawing-1.sketchpad.png
		//H:\\real_eclipse\\CS3\\src\\polarMaker\\TransparentBackgroundSun2.png
		//H:\\real_eclipse\\CS3\\src\\polarMaker\\penguin.png
		File imgFile = new File("H:\\real_eclipse\\CS3\\src\\polarMaker\\penguin.png");
		BufferedImage exampleImg;
		try {
		    FileInputStream fis = new FileInputStream(imgFile);  
			exampleImg = ImageIO.read(fis);
			System.out.println("Image read.");
		} catch(IOException e) {
			System.out.println(e);
			return;
		}
		System.out.println(exampleImg);
		
		
		Polygon shape = ShapeAnalyzer.imageToPolygon(exampleImg);
		for(int i=0; i<shape.npoints; i++ ) {
			shape.ypoints[i] +=200;
			shape.xpoints[i] +=400;
			//System.out.println(shape.xpoints[i]+" "+shape.ypoints[i]);
		}
		
		//test polygon
		/*
		Polygon square = new Polygon();
		square.addPoint(100, 200);
		square.addPoint(100, 400);
		square.addPoint(300, 400);
		square.addPoint(300, 200);*/

		Polygon drillPath = ShapeAnalyzer.buildDrillPath(shape, 8);
		System.out.println("Done builiding the "+drillPath.npoints+" point drill path");
		
		CNCSpecs specs = new CNCSpecs(200, 600, 60, 600, 400, 2);
		ArrayList<Vector2> polarCords = ShapeAnalyzer.polygonToPolar(drillPath, specs);
		System.out.println("converted polygon to polar:");
		for(Vector2 cord : polarCords) System.out.println(cord);
		
		System.out.println("Ther are "+polarCords.size()+" polar cooridnates");
		ArrayList<Vector2> cncCords = ShapeAnalyzer.quantizeCords(polarCords, specs);
		System.out.println("Quantized coordinates: ");
		for(Vector2 cord : cncCords) System.out.println(cord);

		
		System.out.println("There are "+cncCords.size()+" plotted points on the drill path");
		Polygon polygonRep = ShapeAnalyzer.cncCordsToPolygon(cncCords, specs);
		ShapeAnalyzer.addStart(cncCords, specs);
		ArrayList<Integer> problemPoints = ShapeAnalyzer.uninterpolatedPoints(cncCords);
		System.out.println("There are "+problemPoints.size()+" points that need to have interpolation");
		ShapeAnalyzer.interpolatePath(cncCords, problemPoints);
		ArrayList<Integer> extremelyTroublesomePoints = ShapeAnalyzer.uninterpolatedPoints(cncCords);
		System.out.println("There are "+extremelyTroublesomePoints.size()+" extremely troublesome points.");
		System.out.println("Interpolated Cooridnates: ");
		for(Vector2 cord : cncCords) System.out.println(cord);
		
		//useless cycling
		/*
		System.out.println("cycling the methods 5 times.");
		int cycles = 0;
		while(ShapeAnalyzer.uninterpolatedPoints(cncCords).size()!=0) {
			if(cycles == 5) {
				System.out.println("failed after 5 extra cycles leaving "+ShapeAnalyzer.uninterpolatedPoints(cncCords).size()+" meddlesome points");
				break;
			}
			ShapeAnalyzer.interpolatePath(cncCords, ShapeAnalyzer.uninterpolatedPoints(cncCords));
			cycles++;
			
		}*/
		
		/*
		CNCSpecs testSpecs = new CNCSpecs(4, 5, 0, 0, 0, 1);
		ArrayList<Vector2> testCords = new ArrayList<Vector2>();
		testCords.add(new Vector2(2, 0));
		testCords.add(new Vector2(1, 3));
		testCords.add(new Vector2(3, 4));
		System.out.println("Starting test coords:");
		for(int i=0; i<testCords.size(); i++)System.out.println("  "+testCords.get(i));
		ShapeAnalyzer.interpolatePath(testCords, ShapeAnalyzer.uninterpolatedPoints(testCords));
		System.out.println("Interpolated test coords:");
		for(int i=0; i<testCords.size(); i++)System.out.println("  "+testCords.get(i));
		System.out.print("there are "+ShapeAnalyzer.uninterpolatedPoints(testCords).size()+" uninterpolated points remaining");
		*/ //testing stuff
		if(ShapeAnalyzer.uninterpolatedPoints(cncCords).size()!=0) {
			System.out.println("Interpolation failure");
			return;
		}
		
		System.out.println("Succesful cnc path created.");
		System.out.println("Creating Stepper file...");
		File output = new File("PenguinSteps");
		output.createNewFile();
		FileWriter outputWriter = new FileWriter(output);
		ShapeAnalyzer.writePath(outputWriter, cncCords);
		
		
		
		for(int i=0; i<drillPath.npoints; i++ ) {
			//drillPath.ypoints[i] +=100;
			//drillPath.xpoints[i] += 300;
		}
		
		JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.GREEN);
                g.drawPolygon(shape);
                g.setColor(Color.RED);
                g.drawPolygon(drillPath);
                
                g.fillOval(596, 396, 8, 8);
                g.setColor(Color.BLUE);
                g.fillOval(drillPath.xpoints[0]-4, drillPath.ypoints[0]-4, 8, 8);
                g.drawPolygon(polygonRep);
                //Printing some lines
                /*
                g.setColor(Color.BLUE);
                for(int i=0; i<polarCords.size(); i+=20) {
                	g.drawLine(600, 400, 600+(int)(polarCords.get(i).r*Math.cos(polarCords.get(i).theta)), 400+(int)(polarCords.get(i).r*Math.sin(polarCords.get(i).theta)));
                }*/
                
                //System.out.println("I drew the polygon");
            }
            

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(2000, 2000);
            }
        };
        p.setLayout(new FlowLayout());
        try {
			//p.add(new JLabel(new ImageIcon(resizeImage(exampleImg, 500, 500))));
		} catch(Exception e) {
			System.out.println(e);
		}
        
        gui.add(p);
		gui.pack();
        
		gui.setVisible(true);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//JPanel test
	}
	
	
	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
	    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
	    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
	    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
	    return outputImage;
	}
	
	
}
