package bdv.jogl.VolumeRenderer.utils;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.convertToJoglTransform;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.getDataBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.realtransform.AffineTransform3D;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

import bdv.BigDataViewer;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;


/**
 * class to store the volume data and unified access
 * @author michael
 *
 */
public class VolumeDataManager {

	private final Map<Integer, VolumeDataBlock> volumes = new HashMap<Integer, VolumeDataBlock>();
	
	private final Map<Integer, Integer> timestamps = new HashMap<Integer, Integer>();
	
	private final Map<Integer, Boolean> enabled =new HashMap<Integer, Boolean>();
	
	private float globalMaxVolume = 0;
	
	private int globalMaxOccurance = 0;
	
	private int currentTime = -1;
	
	private final BigDataViewer bdv;
	
	private List<IVolumeDataManagerListener> listeners = new ArrayList<IVolumeDataManagerListener>();

	private float minGlobalValue;
	
	public VolumeDataManager(final BigDataViewer bdv){
		this.bdv = bdv;
	}
	
	private void fireAllRemovedData(Integer i ){
		for(IVolumeDataManagerListener l:listeners){
			fireRemovedData(i, l);
		}
	}
	
	
	private void fireRemovedData(Integer i, IVolumeDataManagerListener l){
		l.dataRemoved(i);
	}
	
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
		minGlobalValue = Float.MAX_VALUE;
		
		for(VolumeDataBlock data: volumes.values()){
			
			globalMaxOccurance = Math.max(globalMaxOccurance, data.getMaxOccurance());
			
			if(data.getValueDistribution().isEmpty()){
				continue;
			}
			Float cmax =data.getValueDistribution().lastKey();
			Float cmin = data.getValueDistribution().firstKey();
			globalMaxVolume = Math.max(globalMaxVolume,cmax.floatValue());
			minGlobalValue = Math.max(0.0f,Math.min(minGlobalValue, cmin.floatValue()));
		}
	}
	
	/**
	 * @return the currentTime
	 */
	public int getCurrentTime() {
		return currentTime;
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

	public boolean isEnabled(int i){
		return enabled.get(i);
	}
	
	public VolumeDataBlock getVolume(Integer i) {
		return volumes.get(i);
	}

	public void forceVolumeUpdate(Integer i, int time, VolumeDataBlock data){
		boolean isAllreadyPresent = volumes.containsKey(i); 
		if(time != this.currentTime){
			currentTime = time;
		}
		
		volumes.put(i, data);
		timestamps.put(i, time);
		enabled.put(i, true);
		updateGlobals();
		if(!isAllreadyPresent){
			fireAllAddedData(i);
		}
		
		fireAllUpdatedData(i);
	}
 
	public void setVolume(Integer i, int time , VolumeDataBlock data){
		if(time != this.currentTime){
			currentTime = time;
		}
		boolean sameTime = (timestamps.containsKey(i))?(timestamps.get(i) == time):false;
		boolean contained = volumes.containsKey(i);
		if(sameTime){
			if(contained){
				return;
			}
		}
		
		volumes.put(i, data);
		timestamps.put(i, time);
		enabled.put(i, true);
		updateGlobals();
		
		if(!contained){
			fireAllAddedData(i);
		}	
		fireAllUpdatedData(i);
	}
	
	private void fireAllUpdatedData(Integer i) {
		for(IVolumeDataManagerListener listener : listeners){
			fireUpdatedData(i,listener);
		}
	}

	private void fireUpdatedData(int i,IVolumeDataManagerListener listener) {
		listener.dataUpdated(i);
	}

	public float getGlobalMaxOccurance() {
		return globalMaxOccurance;
	}

	public Collection<VolumeDataBlock> getVolumes() {
		return volumes.values();
	}
	
	public void removeVolumeByIndex(int i) {
		VolumeDataBlock removedData = volumes.remove(i);
		if(removedData != null){
			fireAllRemovedData(i);
		}
	}
	
	public void enableVolume(int i , boolean flag){
		if(!enabled.containsKey(i)){
			return;
		}
		enabled.put(i, flag);
		fireAllDataEnabled(i,flag);
	}
	
	private void fireAllDataEnabled(int i, boolean flag) {
		for(IVolumeDataManagerListener l:listeners){
			fireDataEnabled(l, i,  flag);
		}
		
	}

	private void fireDataEnabled(IVolumeDataManagerListener l, int i,
			boolean flag) {
		l.dataEnabled(i,flag);
		
	}

	public void addVolumeDataManagerListener(IVolumeDataManagerListener l ){
		listeners.add(l);
		for(int i: volumes.keySet()){
			fireAddedData(i, l);
			fireUpdatedData(i, l);
		}
	}
	
	public void updateData(){	
		ViewerState state = bdv.getViewer().getState();
		int currentTimepoint = state.getCurrentTimepoint();
		if(getCurrentTime() == currentTimepoint){
			return;
		}
		
		resetVolumeData();
	}
	
	public void resetVolumeData() {
		//mainly for new time points and data not realy for transform
		ViewerState state = bdv.getViewer().getState();
		List<SourceState<?>> sources = state.getSources();
		int currentTimepoint = state.getCurrentTimepoint();
		
		
		int i =-1;
		for(SourceState<?> source : sources){

			i++;

			//block transform
			int midMapLevel = source.getSpimSource().getNumMipmapLevels()-1;
			VolumeDataBlock data = getDataBlock(bdv,new AABBox(new float[]{0,0,0,0},new float[]{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE}),i,midMapLevel);
			forceVolumeUpdate(i,currentTimepoint, data);
		
		}
		
	}

	public float getGlobalLowestVolumeValue() {
		
		return minGlobalValue;
	}
}
