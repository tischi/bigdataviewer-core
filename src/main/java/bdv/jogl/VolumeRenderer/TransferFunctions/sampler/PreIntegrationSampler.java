package bdv.jogl.VolumeRenderer.TransferFunctions.sampler;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.TreeMap;

import com.jogamp.common.nio.Buffers;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.interpreter.PreIntegrationInterpreter;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import static bdv.jogl.VolumeRenderer.utils.WindowUtils.getNormalizedColor;

public class PreIntegrationSampler implements ITransferFunctionSampler {

	private final PreIntegrationInterpreter desampler = new PreIntegrationInterpreter();

	@Override
	public IFunction getShaderCode() {
		return desampler;
	}

	/**
	 * Returns the integral of a product linear absorbing and color funktion
	 * @param c1 color at begin 
	 * @param c2 color at end 
	 * @param a1 absorbation at begin
	 * @param a2 absorbation at end
	 * @param stepSize slap size
	 * @return
	 */
	private float calcColorChannelIntegral(float c1, float c2, float a1, float a2, float stepSize){
		return stepSize/3.f *(c1*a1+(c1*a2+c2*a1)/2.f +c2*a2);
	}
	
	/**
	 * Returns the integral of a linear absorbation function in its ports
	 * @param a1 absorbation at begin
	 * @param a2 absorbation at end
	 * @param stepSize slap size
	 * @return
	 */
	private float calcAbsorbationIntegral(float a1, float a2, float stepSize){
		return stepSize/2.f * (a1+a2);
	}
	
	
	@Override
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep) {
		//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
		TreeMap<Integer, Color> colorMap = transferFunction.getTexturColor();
		//get Buffer last key is the highest number 
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(((colorMap.lastKey()-colorMap.firstKey())+1)*4);

		//make samples
		Integer intervalBegin = colorMap.firstKey();
		float[] integral = {0,0,0,0};
		buffer.put(integral.clone());
		//iterate candidates
		for(Integer intervalEnd: colorMap.keySet()){
			if(intervalEnd == intervalBegin){
				continue;
			}

			float[] intervalBeginColor = getNormalizedColor(colorMap.get(intervalBegin));
			float[] intervalEndColor = getNormalizedColor(colorMap.get(intervalEnd));
			float[] intervalColorGradient = {0,0,0,0};
			
			//forward difference
			for(int dim = 0; dim < intervalColorGradient.length; dim++){
				intervalColorGradient[dim] = (intervalEndColor[dim]-intervalBeginColor[dim])/((float)(intervalEnd-intervalBegin));
			}
			
			//get upper border of the integral
			float[] sampleColor = intervalBeginColor.clone();
			float[] formerSampleColor = intervalBeginColor.clone();
		
			//sample linear
			for(float step = intervalBegin; step < intervalEnd; step+=sampleStep ){
				
				//increment color
				for(int i =0; i< sampleColor.length; i++){
					sampleColor[i]+= intervalColorGradient[i]*sampleStep;
				}
				
				//absorbation integral
				integral[3] += calcAbsorbationIntegral(formerSampleColor[3], sampleColor[3], sampleStep);
				
				//color integral
				for(int i = 0; i< 3; i++){
					integral[i] +=calcColorChannelIntegral(formerSampleColor[i], sampleColor[i], 
							formerSampleColor[3], sampleColor[3], sampleStep);
				}
				buffer.put(integral.clone());
				formerSampleColor = sampleColor.clone();
			}
			intervalBegin = intervalEnd;
		}
		buffer.rewind();
		return buffer;
	}
}
