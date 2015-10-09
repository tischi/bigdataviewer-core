package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.scvMaxNumberOfVolumes;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvRayPositions;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.sgvVolumeNormalizeFactor;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.calculateCurvatureOfVolume;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.createVolumeTexture;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.CurvatureContainer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

/**
 * Calculates the maximum difference of the curvature values
 * @author michael
 *
 */
public class MaxCurvatureDifference extends AbstractVolumeAccumulator {
	public MaxCurvatureDifference() {
		super("curvature_difference");
	}
	
	private boolean needsReset = true;
	
	private final Map<Integer, Texture> curvatureTexture = new HashMap<Integer, Texture>();
	
	private final Map<Integer, CurvatureContainer> evaluatedCurvatures = new HashMap<Integer, CurvatureContainer>();
	
	private final static String suvCurvatureTexture = "inCurvatureTexture"; 
	
	private final static String suvCurvatureMax = "inCurvatureMax";

	private final static String suvCurvatureMin = "inCurvatureMin";
	@Override
	public void init(GL4 gl) {
		getParent().mapUniforms(gl,new String[]{suvCurvatureTexture});
		getParent().mapUniforms(gl,new String[]{suvCurvatureMax});
		getParent().mapUniforms(gl,new String[]{suvCurvatureMin});
		super.init(gl);
	}

	@Override
	public void disposeGL(GL4 gl2) {
		for(Texture t: curvatureTexture.values()){
			t.delete(gl2);
		}
		curvatureTexture.clear();
		evaluatedCurvatures.clear();
		super.disposeGL(gl2);
	}
	
	@Override
	public void updateData(GL4 gl) {
		VolumeDataManager dataManager = getParent().getDataManager();
		float globalMin = Float.MAX_VALUE;
		float globalMax = Float.MIN_VALUE;
		
		//create and update laplace textures 
		for(Integer key:dataManager.getVolumeKeys()){
			VolumeDataBlock data = dataManager.getVolume(key);
			Texture t;
			
			//create texture objects
			if(!curvatureTexture.containsKey(key)||needsReset){
				t = createVolumeTexture(gl, getParent().getArrayEntryLocation(gl, suvCurvatureTexture,key)) ;
				curvatureTexture.put(key, t);
			}
			t = curvatureTexture.get(key);
			
			//update laplacians
			if(data.needsUpdate()||!evaluatedCurvatures.containsKey(key)||needsReset){
				CurvatureContainer container = calculateCurvatureOfVolume(data); 
				evaluatedCurvatures.put(key,container);
				
			
				//fill textures
				FloatBuffer buffer = Buffers.newDirectFloatBuffer(container.valueMesh3d);
				t.update(gl, 0, buffer, container.dimension);
				
			}
			CurvatureContainer tmp = evaluatedCurvatures.get(key);
			
			//get global min max value of laplace
			globalMax = Math.max(globalMax, tmp.maxValue);
			globalMin = Math.min(globalMin, tmp.minValue);
		}
		//update globals
		gl.glUniform1f(getParent().getLocation(suvCurvatureMin), globalMin);
		gl.glUniform1f(getParent().getLocation(suvCurvatureMax), globalMax);
		
		needsReset = false;
		super.updateData(gl);
	}
	
	
	
	@Override
	public void setParent(MultiVolumeRenderer parent) {
		needsReset = true;
		super.setParent(parent);
	}
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 6666",
				"uniform sampler3D "+suvCurvatureTexture+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvCurvatureMax+";",
				"uniform float "+suvCurvatureMin+";",
				"float curveNormalizeFactor = 1.0/("+suvCurvatureMax+"-"+suvCurvatureMin+");",
				"bool factorChanged = false;",
				"",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float minValue = "+Float.MAX_VALUE+";",
				"	float maxValue = "+Float.MIN_VALUE+";",			
				"   if(!factorChanged){",
				//TODO
				"		"+sgvVolumeNormalizeFactor+" = curveNormalizeFactor;",
				"		factorChanged = true;",
				"	}",
				"	for(int n = 0; n < "+scvMaxNumberOfVolumes+"; n++){",
				"		if(densities[n] < 0.0 ){",
				"			continue;",
				"		}",	
				"		vec3 texCN = getCorrectedTexturePositions("+sgvRayPositions+", n);",
				"		float cn = texture("+suvCurvatureTexture+"[n],texCN).r;",
				"		maxValue = max(maxValue,cn);",
				"		minValue = min(minValue,cn);",	
				"	}",
				"	return maxValue -minValue;",	
				"}"
		};
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}
}
