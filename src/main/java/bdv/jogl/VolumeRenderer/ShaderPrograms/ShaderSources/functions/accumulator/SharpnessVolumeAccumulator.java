package bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;

import bdv.jogl.VolumeRenderer.Scene.Texture;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.utils.LaplaceContainer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import static bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.MultiVolumeRendererShaderSource.*;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.addCodeArrayToList;
import static bdv.jogl.VolumeRenderer.utils.ShaderSourceUtil.appendNewLines;
import static bdv.jogl.VolumeRenderer.utils.VolumeDataUtils.*;
import static bdv.jogl.VolumeRenderer.utils.GLUtils.*;

/**
 * Accumulator based on the laplacian weights
 * @author michael
 *
 */
public class SharpnessVolumeAccumulator extends AbstractVolumeAccumulator {

	public SharpnessVolumeAccumulator() {
		super("sharpness_weight");
	}
	
	Map<Integer, LaplaceContainer> evalueatedLaplacians = new HashMap<Integer, LaplaceContainer>();
	
	Map<Integer, Texture> laplacianTextures = new HashMap<Integer, Texture>();
	
	private final static String suvLaplaceTextures = "inLaplaceTextures";
	
	private final static String suvLaplaceMinValue = "intLaplaceMin";
	
	private final static String suvLaplaceMaxValue = "intLaplaceMax";
	
	boolean needsReset = false;
	
	@Override
	public String[] declaration() {
		List<String> code = new ArrayList<String>();
		String[] dec= new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 909",
				"float "+getFunctionName()+"(float densities["+scvMaxNumberOfVolumes+"]) {",
				"	float density = 0.0;",		
				"	float sum = 0.0;",
				"	const int N = "+scvMaxNumberOfVolumes+";",
				"	float weights[N] = calcWeights();",
				"	for(int n = 0; n< N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		sum+=weights[n];",
				"	}",	
				"	for(int n = 0; n < N; n++){",
				"		if(densities[n] < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = (weights[n]/sum);",
				"		density += weight * densities[n];",
				"	}",
				"	density = density;",
				"	return density;",	
				"}"
		};
		addCodeArrayToList(colorAccDecl(), code);
		addCodeArrayToList(dec, code);
		String[] codeArray = new String[code.size()];
		code.toArray(codeArray);
		appendNewLines(codeArray);
		return codeArray;
	}

	@Override
	protected String[] colorAccDecl() {
		// TODO Auto-generated method stub
		return new String[]{
				"#line "+Thread.currentThread().getStackTrace()[1].getLineNumber()+ " 908",
				"uniform sampler3D "+suvLaplaceTextures+"["+scvMaxNumberOfVolumes+"];",
				"uniform float "+suvLaplaceMinValue+";",
				"uniform float "+suvLaplaceMaxValue+";",
				"float laplaceNormFactor = 1.0 /("+suvLaplaceMaxValue+"-"+suvLaplaceMinValue+");",
				"",
				"float ["+scvMaxNumberOfVolumes+"] calcWeights(){",
				"	float weights["+scvMaxNumberOfVolumes+"];",
				"	for(int v = 0; v < "+scvMaxNumberOfVolumes+"; v++){",
				"		vec3 texC = getCorrectedTexturePositions("+sgvRayPositions+"[v], v);",
				"		weights[v] =  laplaceNormFactor*(texture("+suvLaplaceTextures+"[v],texC).r +"+suvLaplaceMinValue+") ;",
				"	}",
				"	return weights;",
				"}",
				"",
				"vec3 "+getColorFunctionName()+"(vec4 colors["+scvMaxNumberOfVolumes+"],vec4 refinedValues["+scvMaxNumberOfVolumes+"]){",
				"	vec3  color = vec3(0.0);",		
				"	float sum = 0.0;",
				"	const int N = "+scvMaxNumberOfVolumes+";",
				"	float weights[N] = calcWeights();",
				"	for(int n = 0; n< N; n++){",
				"		if(refinedValues[n].a < 0.0){",
				"			continue;",	
				"		}",	
				"		sum+=weights[n];",
				"	}",	
				"	for(int n = 0; n < N; n++){",
				"		if(refinedValues[n].a < 0.0){",
				"			continue;",	
				"		}",	
				"		float weight = (weights[n]/sum);",
				"		color += weight * colors[n].rgb;",
				"	}",
				"	color = color;",
				"	return color;",	
				"}"
				};
	}
	
	@Override
	public void init(GL4 gl) {
		getParent().mapUniforms(gl,new String[]{suvLaplaceTextures});
		getParent().mapUniforms(gl,new String[]{suvLaplaceMinValue});
		getParent().mapUniforms(gl,new String[]{suvLaplaceMaxValue});
		super.init(gl);
	}

	@Override
	public void disposeGL(GL4 gl2) {
		for(Texture t: laplacianTextures.values()){
			t.delete(gl2);
		}
		laplacianTextures.clear();
		evalueatedLaplacians.clear();
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
			if(!laplacianTextures.containsKey(key)||needsReset){
				t = createVolumeTexture(gl, getParent().getLocation(suvLaplaceTextures)+key) ;
				laplacianTextures.put(key, t);
			}
			t = laplacianTextures.get(key);
			
			//update laplacians
			if(data.needsUpdate()||!evalueatedLaplacians.containsKey(key)||needsReset){
				LaplaceContainer container = calulateLablacianSimple(data); 
				evalueatedLaplacians.put(key,container);
				
				//check wethe sparse textures should be used
				boolean isSparseData=false;
				for(int i = 0; i < data.dimensions.length; i++){
					if(data.memOffset[i] != 0 || data.memSize[i] != data.dimensions[i]){
						isSparseData =true;
						break;
					}
				}
			
				//fill textures
				FloatBuffer buffer = Buffers.newDirectFloatBuffer(container.valueMesh3d);
				if(isSparseData){
					int memData[][] = new int[3][3];
					long tmp[] = calculateSparseVirtualTextures(gl, t, data.dimensions);
					for(int j = 0; j < 3 ; j++){
						memData[0][j] = (int)tmp[j];
						memData[1][j] = (int)data.memOffset[j];
						memData[2][j] = (int)data.memSize[j];
					}
					t.updateSparse(gl, 0, buffer, memData[0], memData[1], memData[2]);
				}else{
					t.update(gl, 0, buffer, container.dimension);
				}
			}
			LaplaceContainer tmp = evalueatedLaplacians.get(key);
			
			//get global min max value of laplace
			globalMax = Math.max(globalMax, tmp.maxValue);
			globalMin = Math.min(globalMin, tmp.minValue);
		}
		//update globals
		gl.glUniform1f(getParent().getLocation(suvLaplaceMinValue), globalMin);
		gl.glUniform1f(getParent().getLocation(suvLaplaceMaxValue), globalMax);
		
		needsReset = false;
		super.updateData(gl);
	}
	
	
	
	@Override
	public void setParent(MultiVolumeRenderer parent) {
		needsReset = true;
		super.setParent(parent);
	}
}

