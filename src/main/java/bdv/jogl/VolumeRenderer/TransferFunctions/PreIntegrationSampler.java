package bdv.jogl.VolumeRenderer.TransferFunctions;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.TreeMap;

import com.jogamp.common.nio.Buffers;

import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IFunction;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.PreIntegrationInterpreter;

public class PreIntegrationSampler implements ITransferFunctionSampler {

	private final PreIntegrationInterpreter desampler = new PreIntegrationInterpreter();

	@Override
	public IFunction getShaderCode() {
		return desampler;
	}

	@Override
	public FloatBuffer sample(TransferFunction1D transferFunction, float sampleStep) {
		//http://www.uni-koblenz.de/~cg/Studienarbeiten/SA_MariusErdt.pdf
		TreeMap<Integer, Color> colorMap = transferFunction.getTexturColor();
		//get Buffer last key is the highest number 
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(((colorMap.lastKey()-colorMap.firstKey())+1)*4);
		
		buffer.put(new float[]{0,0,0,0});

		//make samples
		Integer intervalBegin = colorMap.firstKey();
		float[] integral = {0,0,0,0};
		//iterate candidates
		for(Integer intervalEnd: colorMap.keySet()){
			if(intervalEnd == intervalBegin){
				continue;
			}

			float[] intervalBeginColor = {0,0,0,(float)(colorMap.get(intervalBegin).getAlpha())/255.f};
			float[] intervalEndColor = {0,0,0,(float)(colorMap.get(intervalEnd).getAlpha())/255.f};
			float[] intervalColorGradient = {0,0,0,0};
			colorMap.get(intervalBegin).getColorComponents(intervalBeginColor);
			colorMap.get(intervalEnd).getColorComponents(intervalEndColor);
			
			//forward difference
			for(int dim = 0; dim < intervalColorGradient.length; dim++){
				intervalColorGradient[dim] = (intervalEndColor[dim]-intervalBeginColor[dim])/(intervalEnd-intervalBegin);
			}
			
			float[] formerSampleColor = intervalBeginColor.clone();
		
			//sample linear
			for(Integer step = intervalBegin; step < intervalEnd; step++ ){
				
				//get upper border of the integral
				float[] sampleColor = intervalBeginColor.clone();
				for(int i =0; i< sampleColor.length; i++){
					sampleColor[i]+= intervalColorGradient[i];
				}
				
				//alpha of mean integral
				float gx1 = formerSampleColor[3];
				float gx2 = sampleColor[3];
				float currentAbsorbtionIntegral = sampleStep/2.f * (gx1+gx2);
				integral[3] += currentAbsorbtionIntegral;
				
				//color integral
				for(int i = 0; i< 3; i++){
					float fx1 = formerSampleColor[i];
					float fx2 = sampleColor[i];
					float componetColorIntegral=  sampleStep/3.f *(fx1*gx1+(fx1*gx2+fx2*gx1)/2.f +fx2*gx2) ;
					integral[i] += componetColorIntegral;
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
