package bdv.jogl.VolumeRenderer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * class to store the volume data and unified access
 * @author michael
 *
 */
public class VolumeDataManager {

	private final Map<Integer, VolumeDataBlock> volumes = new HashMap<Integer, VolumeDataBlock>();
	
	private float globalMaxVolume = 0;
	
	private int globalMaxOccurance = 0;
	
	private void updateGlobals(){
		globalMaxVolume = 0;
		globalMaxOccurance = 0;
		
		for(VolumeDataBlock data: volumes.values()){
			
			globalMaxOccurance = Math.max(globalMaxOccurance, data.getMaxOccurance());
			
			Float cmax =data.getValueDistribution().lastKey();
			if(cmax == null){
				continue;
			}
			globalMaxVolume = Math.max(globalMaxVolume,cmax.floatValue());
		}
	}
	
	/**
	 * Returns the maximal volume value of the currently stored volume values
	 * @return
	 */
	public float getGlobalMaxVolumeValue(){
		return globalMaxVolume;
	}

	public Set<Integer> getVolumeKeys() {
		return volumes.keySet();
	}

	public VolumeDataBlock getVolume(Integer i) {
		return volumes.get(i);
	}

 
	public void setVolume(Integer i, VolumeDataBlock data){
		volumes.put(i, data);
		updateGlobals();
	}
	
	public float getGlobalMaxOccurance() {
		return globalMaxOccurance;
	}
}
