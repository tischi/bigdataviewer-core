package bdv.jogl.VolumeRenderer.gui;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

/**
 * empty implementation of the listener to not waste code
 * @author michael
 *
 */
public abstract class TransferFunctionAdapter implements
		TransferFunctionListener {

	@Override
	public void colorChanged(TransferFunction1D transferFunction) {

	}

	@Override
	public void samplerChanged(TransferFunction1D transferFunction1D) {

	}

}
