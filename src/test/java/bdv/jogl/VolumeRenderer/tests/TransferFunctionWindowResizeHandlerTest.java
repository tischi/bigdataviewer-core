package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import bdv.jogl.VolumeRenderer.gui.TransferFunctionWindowResizeHandler;

public class TransferFunctionWindowResizeHandlerTest {

	private JPanel testPanel;
	
	private Dimension[] testSizes;
	
	private TreeMap<Integer, Integer> testPointMap;
	
	private TreeMap<Integer, Color> testColorMap;
	
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
		testPointMap = new TreeMap<Integer, Integer>();
		testColorMap = new TreeMap<Integer, Color>();
		testSizes = new Dimension[]{new Dimension(51,101),new Dimension(104,17),
				new Dimension(11,1000),new Dimension(640,480),new Dimension(200,100)};
		
		
		testPanel = new JPanel();
	}
	
	private void pointBorderTests(Dimension size) throws InterruptedException{
		testPanel.setSize(size);
		
		//sync
		Boolean check = sync.poll(1, TimeUnit.SECONDS);
		assertTrue(check);
		
		assertTrue(testPointMap.containsKey(0));
		assertEquals(0, testPointMap.get(0).intValue());
		
		assertTrue("schearched for: " +size.width+" in: "+testPointMap,testPointMap.containsKey(size.width));
		assertEquals(size.height, testPointMap.get(size.width).intValue());
	}
	
	private void colorBorderTests(Dimension size) throws InterruptedException{
		testPanel.setSize(size);
		
		//sync
		Boolean check = sync.poll(1, TimeUnit.SECONDS);
		assertTrue(check);
		
		assertTrue(testColorMap.containsKey(0));
		assertEquals(Color.BLUE, testColorMap.get(0));
		
		assertTrue("schearched for: " +size.width+" in: "+testColorMap,testColorMap.containsKey(size.width));
		assertEquals(Color.RED, testColorMap.get(size.width));
	}
	@Test
	public void borderPointsTest() throws InterruptedException {
		Dimension testBeginSize = new Dimension(73,83 );
		testPanel.setSize(testBeginSize);
		testPointMap.put(0,0);
		testPointMap.put(testPanel.getWidth(), testPanel.getHeight());
		objectUnderTest = new TransferFunctionWindowResizeHandler(testPanel.getSize(), testColorMap, testPointMap);
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
		testColorMap.put(0,Color.BLUE);
		testColorMap.put(testPanel.getWidth(),Color.red);
		objectUnderTest = new TransferFunctionWindowResizeHandler(testPanel.getSize(), testColorMap, testPointMap);
		testPanel.addComponentListener(objectUnderTest);
		testPanel.addComponentListener(syncListener);
		
		//do resize
		for(Dimension dim :testSizes){
			colorBorderTests(dim);
		}
	}
	
	@After
	public void tearDown(){
		testColorMap = null;
		testColorMap = null;
		testPanel = null;
		testSizes =null;
	}

}
