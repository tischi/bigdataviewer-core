package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.gui.TransferFunctionWindowResizeHandler;

public class TransferFunctionWindowResizeHandlerTest {

	private JPanel testPanel;
	
	private Dimension[] testSizes;
	
	private TransferFunction1D testTransferFunction;
	
	private TransferFunctionWindowResizeHandler objectUnderTest;
	
	private BlockingQueue<Boolean> sync = new ArrayBlockingQueue<Boolean>(1);
	
	private ComponentListener syncListener = new ComponentListener() {
		
		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void componentResized(ComponentEvent e) {
			try {
				sync.put(true);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Before
	public void setup(){
		Dimension testBeginSize = new Dimension(73,83 );
		testPanel = new JPanel();
		testPanel.setSize(testBeginSize);
		testTransferFunction = new TransferFunction1D(testBeginSize.width, testBeginSize.height);
		testSizes = new Dimension[]{new Dimension(51,101),new Dimension(104,17),
				new Dimension(11,1000),new Dimension(640,480),new Dimension(200,100)};
		
		
		
	}
	
	private void pointBorderTests(Dimension size) throws InterruptedException{
		testPanel.setSize(size);
		
		//sync
		Boolean check = sync.poll(1, TimeUnit.SECONDS);
		assertTrue(check);
		
		TreeSet<Point> points = testTransferFunction.getFunctionPoints();
		
		Point searchPoint = new Point(0,0);
		assertTrue(points.contains(searchPoint));
		
		searchPoint = new Point(size.width,size.height);
		assertTrue("schearched for: " +searchPoint,points.contains(searchPoint));
	}
	
	private void colorBorderTests(Dimension size) throws InterruptedException{
		testPanel.setSize(size);
		
		//sync
		Boolean check = sync.poll(1, TimeUnit.SECONDS);
		assertTrue(check);
		
		TreeMap<Point, Color> colors = testTransferFunction.getColors();
		
		Point searchPoint = new Point(0,0);
		assertTrue(colors.containsKey(searchPoint));
		assertEquals(Color.BLUE, colors.get(searchPoint));
		
		searchPoint = new Point(size.width,size.height);
		assertTrue("schearched for: " +searchPoint+ " in " +colors,colors.containsKey(searchPoint));
		assertEquals(Color.RED, colors.get(searchPoint));
	}
	@Test
	public void borderPointsTest() throws InterruptedException {

		testTransferFunction.addFunctionPoint(new Point(0,0));
		testTransferFunction.addFunctionPoint(new Point(testPanel.getWidth(), testPanel.getHeight()));
		objectUnderTest = new TransferFunctionWindowResizeHandler(testPanel.getSize(), testTransferFunction);
		testPanel.addComponentListener(objectUnderTest);
		testPanel.addComponentListener(syncListener);
		
		//do resize
		for(Dimension dim :testSizes){
			pointBorderTests(dim);
		}
	}
	
	
	@Test 
	public void borderColorTest() throws InterruptedException{
		Dimension testBeginSize = new Dimension(73,83 );
		testPanel.setSize(testBeginSize);
		testTransferFunction.setColor(new Point(0,0),Color.BLUE);
		testTransferFunction.setColor(new Point(testPanel.getWidth(),testPanel.getHeight()),Color.red);
		objectUnderTest = new TransferFunctionWindowResizeHandler(testPanel.getSize(), testTransferFunction);
		testPanel.addComponentListener(objectUnderTest);
		testPanel.addComponentListener(syncListener);
		
		//do resize
		for(Dimension dim :testSizes){
			colorBorderTests(dim);
		}
	}
	
	@After
	public void tearDown(){
		testPanel = null;
		testSizes =null;
	}

}
