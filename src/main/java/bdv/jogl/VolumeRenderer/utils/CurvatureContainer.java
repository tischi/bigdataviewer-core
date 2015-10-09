package bdv.jogl.VolumeRenderer.utils;

import java.util.TreeMap;

public class CurvatureContainer {
	
	public float[] valueMesh3d;
	public final TreeMap<Float,Integer> distribution = new TreeMap<Float, Integer>(); 
	public float minValue = Float.MAX_VALUE;
	public float maxValue = Float.MIN_VALUE;
	public int dimension[]; 
}
