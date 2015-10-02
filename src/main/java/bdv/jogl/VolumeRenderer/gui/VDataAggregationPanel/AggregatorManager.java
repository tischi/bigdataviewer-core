package bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxCurvatureDifference;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MidmapMaxDifference;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MinimumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.SharpnessVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.ViewDirectionAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.VoxelDistanceAccumulator;

/**
 * Stores aggregators and calls listeners
 * @author michael
 *
 */
public class AggregatorManager {
	
	private List<IVolumeAggregationListener> listeners = new ArrayList<IVolumeAggregationListener>();
	
	private Map<String, AbstractVolumeAccumulator> accumulators = new HashMap<String, AbstractVolumeAccumulator>();
	
	private String activeAccumulatorName;
	
	private void notifyChanged(IVolumeAggregationListener listener){
		listener.aggregationChanged(accumulators.get(activeAccumulatorName));
	}
	
	private void notifyChangedAll(){
		for(IVolumeAggregationListener listener: listeners){
			notifyChanged(listener);
		}
	}
	
	private void addAccumulator(AbstractVolumeAccumulator a){
		accumulators.put(a.getFunctionName(), a);
	}
	
	
	
	public AggregatorManager(){
		MaximumVolumeAccumulator max =new MaximumVolumeAccumulator();
		addAccumulator(new AverageVolumeAccumulator());
		addAccumulator(new MaximumVolumeAccumulator());
		addAccumulator(new MinimumVolumeAccumulator());
		addAccumulator(new MaxDifferenceAccumulator());
		addAccumulator(new MidmapMaxDifference());
		addAccumulator(new MaxCurvatureDifference());
	//	addAccumulator(new VoxelDistanceAccumulator());
		addAccumulator(new ViewDirectionAccumulator());
		addAccumulator(new SharpnessVolumeAccumulator());
		setActiveAcumulator(max.getFunctionName());
	}
	
	/**
	 * Adds aggregation listener
	 * @param listener
	 */
	public void addListener(IVolumeAggregationListener listener){
		this.listeners.add(listener);
	} 

	
	/**
	 * Returns the function name of the currently active accumulator
	 * @return
	 */
	public String getActiveAccumulator(){
		return activeAccumulatorName;
	}
	
	
	/**
	 * Set the current active accumulator and calls the changed listener 
	 * @param name
	 */
	public void setActiveAcumulator(String name){
		activeAccumulatorName = name;
		notifyChangedAll();
	}
	
	
	/**
	 * returns a accumulator identified by its funktion name or null if not present
	 * @param name
	 * @return
	 */
	public AbstractVolumeAccumulator getAccumulator(String name){
		return accumulators.get(name);
	};
	
	
	/**
	 * returns the function-names of the stored accumulators 
	 * @return
	 */
	public Set<String> getAccumulatorNames(){
		return accumulators.keySet();
	}
}
