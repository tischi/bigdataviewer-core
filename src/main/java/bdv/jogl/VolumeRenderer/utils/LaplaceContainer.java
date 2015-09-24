package bdv.jogl.VolumeRenderer.utils;

/**
 * container for laplace evaluation
 * @author michael
 *
 */
public class LaplaceContainer{
	public float[] valueMesh3d;
	public float minValue = Float.MAX_VALUE;
	public float maxValue = Float.MIN_VALUE;
	public int dimension[]; 
}
