package bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;

/**
 * Listen on aggregation events
 * @author michael
 *
 */
public interface IVolumeAggregationListener {

	/**
	 * Is called if the accumulation changes
	 * @param acc
	 */
	public void aggregationChanged(AbstractVolumeAccumulator acc);
}
