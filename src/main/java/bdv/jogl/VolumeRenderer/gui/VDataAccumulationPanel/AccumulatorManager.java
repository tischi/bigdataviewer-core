package bdv.jogl.VolumeRenderer.gui.VDataAccumulationPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxCurvatureDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MidmapMaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MinimumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.SharpnessVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.ViewDirectionAccumulator;

/**
 * Stores aggregators and calls listeners
 * @author michael
 *
 */
public class AccumulatorManager {
	
	private List<IVolumeAccumulatorListener> listeners = new ArrayList<IVolumeAccumulatorListener>();
	
	private Map<String, AbstractVolumeAccumulator> accumulators = new HashMap<String, AbstractVolumeAccumulator>();
	
	private List<String> aggregatorNames = new ArrayList<String>();
	
	private String activeAccumulatorName;
	
	private void notifyChanged(IVolumeAccumulatorListener listener){
		listener.aggregationChanged(accumulators.get(activeAccumulatorName));
	}
	
	private void notifyChangedAll(){
		for(IVolumeAccumulatorListener listener: listeners){
			notifyChanged(listener);
		}
	}
	
	private void addAccumulator(AbstractVolumeAccumulator a){
		String visualName = beautifyaccumulatorFuncName(a.getFunctionName());
		accumulators.put(visualName, a);
		aggregatorNames.add(visualName);
	}
	
	
	
	private String beautifyaccumulatorFuncName(String functionName) {
		String beautified =""+ Character.toUpperCase( (char)functionName.getBytes()[0]);
		
		for(int i =1; i < functionName.length(); i++){
			if(functionName.getBytes()[i] == (char)'_'){
				beautified += ' ';
				continue;
			}
			beautified +=  (char)functionName.getBytes()[i];
		}
		return beautified;
	}

	public AccumulatorManager(){
		MaximumVolumeAccumulator max =new MaximumVolumeAccumulator();
	
		addAccumulator(new MaximumVolumeAccumulator());
		addAccumulator(new MinimumVolumeAccumulator());
		addAccumulator(new AverageVolumeAccumulator());
		addAccumulator(new ViewDirectionAccumulator());
		addAccumulator(new SharpnessVolumeAccumulator());
		addAccumulator(new MaxDifferenceAccumulator());
		addAccumulator(new MidmapMaxDifferenceAccumulator());
		addAccumulator(new MaxCurvatureDifferenceAccumulator());
	//	addAccumulator(new VoxelDistanceAccumulator());

		setActiveAcumulator( beautifyaccumulatorFuncName(max.getFunctionName()));
	}
	
	/**
	 * Adds aggregation listener
	 * @param listener
	 */
	public void addListener(IVolumeAccumulatorListener listener){
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
	public List<String> getAccumulatorNames(){
		return aggregatorNames;
	}
}
