package bdv.jogl.VolumeRenderer.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	
	private List<IVolumeDataManagerListener> listeners = new ArrayList<IVolumeDataManagerListener>();
	
	private void fireAddedData(Integer i,IVolumeDataManagerListener l){
		l.addedData(i);
	}
	
	private void fireAllAddedData(Integer i){
		for(IVolumeDataManagerListener l:listeners){
			fireAddedData(i,l);
		}
	}
	
	private void updateGlobals(){
		globalMaxVolume = 0;
		globalMaxOccurance = 0;
		
		for(VolumeDataBlock data: volumes.values()){
			
			globalMaxOccurance = Math.max(globalMaxOccurance, data.getMaxOccurance());
			
			if(data.getValueDistribution().isEmpty()){
				continue;
			}
			Float cmax =data.getValueDistribution().lastKey();
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
		fireAllAddedData(i);
	}
	
	public float getGlobalMaxOccurance() {
		return globalMaxOccurance;
	}

	public Collection<VolumeDataBlock> getVolumes() {
		return volumes.values();
	}

	public void removeVolumeByIndex(int i) {
		volumes.remove(i);
	}
	
	public void addVolumeDataManagerListener(IVolumeDataManagerListener l ){
		listeners.add(l);
	}
}
