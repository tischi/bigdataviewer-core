package bdv.jogl.VolumeRenderer.utils;

import java.util.TreeMap;

import com.jogamp.opengl.math.Matrix4;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;

public class VolumeDataBlock{
	public float[] data;
	public long[] minPoint={0,0,0};
	public long[] maxPoint={0,0,0};
	public long[] dimensions = {0,0,0};
	public float maxValue;
	public float minValue;
	public Matrix4 localTransformation = getNewIdentityMatrix();
	public final TreeMap<Float, Integer> valueDistribution = new TreeMap<Float, Integer>();
	public int maxOccurance = 0;
	private boolean needsUpdate = true;
	
	
	public TreeMap<Float, Integer> getValueDistribution() {
		return valueDistribution;
	}

	public boolean needsUpdate(){
		return needsUpdate;
	}
	
	public void setNeedsUpdate(boolean tag){
		needsUpdate = tag;
	}
	
	public int getMaxOccurance(){
		return maxOccurance;
	}
}