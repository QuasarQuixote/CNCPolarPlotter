package polarMaker;
import java.awt.FlowLayout;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.*;

public class UserGUI {
	public static void main(String[] args) {
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
		Polygon square = new Polygon();
		square.addPoint(100, 200);
		square.addPoint(100, 400);
		square.addPoint(300, 400);
		square.addPoint(300, 200);

		Polygon drillPath = ShapeAnalyzer.buildDrillPath(shape, 8);
		System.out.println("Done builiding the "+drillPath.npoints+" point drill path");
		
		CNCSpecs specs = new CNCSpecs(4, 5, 1, 600, 400, 0.01);
		ArrayList<Vector2> polarCords = ShapeAnalyzer.polygonToPolar(drillPath, specs);
		System.out.println("Ther are "+polarCords.size()+" polar cooridnates");
		ArrayList<Vector2> cncCords = ShapeAnalyzer.quantizeCords(polarCords, specs);
		System.out.println("There are "+cncCords.size()+" plotted points on the drill path");
		ArrayList<Integer> problemPoints = ShapeAnalyzer.uninterpolatedPoints(cncCords);
		System.out.println("There are "+problemPoints.size()+" points that need to have interpolation");

		
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
