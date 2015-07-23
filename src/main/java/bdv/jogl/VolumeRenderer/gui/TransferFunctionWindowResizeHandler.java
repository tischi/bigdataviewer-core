package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.TreeMap;

/**
 * Class handling the rescaling of the transfer function logical data while resizing 
 * @author michael
 *
 */
public class TransferFunctionWindowResizeHandler implements ComponentListener {

	private final Dimension oldSize;
	
	private final TreeMap<Integer, Color> oldColorMap;

	private final TreeMap<Integer, Integer> oldFunctionPoints;
	
	public TransferFunctionWindowResizeHandler(final Dimension oldSize,
			final TreeMap<Integer, Color> oldColorMap,
			final TreeMap<Integer, Integer> oldFunctionPoints) {
		
		this.oldSize = new Dimension(oldSize);
		this.oldColorMap = oldColorMap;
		this.oldFunctionPoints = oldFunctionPoints;
	}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		Dimension newSize = e.getComponent().getSize();
		
		float [] scaleFactors = {(float)newSize.width/(float)oldSize.width, 
				(float)newSize.height/(float)oldSize.height };
		
		//scale color indices
		TreeMap<Integer, Color> newColorMap = new TreeMap<Integer, Color>();
		for(Integer index:oldColorMap.keySet()){
			newColorMap.put(Math.round(scaleFactors[0]*index.floatValue()), oldColorMap.get(index));
		}
		oldColorMap.clear();
		oldColorMap.putAll(newColorMap);
		
		//scale function Points
	
		TreeMap<Integer, Integer> newFunctionPoints = new TreeMap<Integer, Integer>();
		for(Integer index:oldFunctionPoints.keySet()){
			newFunctionPoints.put(Math.round(scaleFactors[0]*index.floatValue()), 
					Math.round(scaleFactors[1]*oldFunctionPoints.get(index).floatValue()));
		}
		oldFunctionPoints.clear();
		oldFunctionPoints.putAll(newFunctionPoints);
		
		
		oldSize.setSize(newSize);

	}

	@Override
	public void componentShown(ComponentEvent e) {}
}
