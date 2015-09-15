package bdv.jogl.VolumeRenderer.utils;

/**
 * listen on events of the Volume data manager
 * @author michael
 *
 */
public interface IVolumeDataManagerListener {

	/**
	 * generic callback if some data changed within the data manager
	 */
	public void addedData(Integer i);
	
	
	public void dataUpdated(Integer i);
	
	public void dataRemoved(Integer i);


	public void dataEnabled(Integer i, Boolean flag);
}
