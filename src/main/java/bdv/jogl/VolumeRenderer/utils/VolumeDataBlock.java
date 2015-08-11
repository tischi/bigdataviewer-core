package bdv.jogl.VolumeRenderer.utils;

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
	private boolean needsUpdate = true;
	
	public boolean needsUpdate(){
		return needsUpdate;
	}
	
	public void setNeedsUpdate(boolean tag){
		needsUpdate = tag;
	}
}