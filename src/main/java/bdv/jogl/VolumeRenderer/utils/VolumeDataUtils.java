package bdv.jogl.VolumeRenderer.utils;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.naming.spi.DirStateFactory;

import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.BigDataViewerDataSelector;
import bdv.viewer.state.SourceState;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;


/**
 * Delivers standard volume data interaction methods
 * @author michael
 *
 */
public class VolumeDataUtils {

	private static Color volumeColor[] = new Color[]{

		Color.YELLOW,
		Color.CYAN,
		Color.DARK_GRAY,
		Color.BLUE,
		Color.GREEN,
		Color.BLACK,
	};


	/**
	 * Gets the volume data from a RandomAccessibleInterval
	 * @param source data to copy from
	 * @return An array of volume data with the dimensions x,y,z 
	 */
	public static VolumeDataBlock getDataBlock(final BigDataViewer bdv,final AABBox minBoundingBox, int sourceId, int midmap){
		SourceState<?> source = bdv.getViewer().getState().getSources().get(sourceId);
		int currentTimePoint = bdv.getViewer().getState().getCurrentTimepoint();
		VolumeDataBlock data = new VolumeDataBlock();
		TreeMap<Float, Integer> distr = data.getValueDistribution();
		RandomAccessibleInterval<?> dataField = source.getSpimSource().getSource(currentTimePoint, midmap);
		IterableInterval<?> tmp = Views.flatIterable(dataField);
		tmp.dimensions( data.dimensions);

		long minMax[][] = new long[][]{{(long)Math.max(Math.floor(minBoundingBox.getMinX()),0),(long)Math.max(Math.floor(minBoundingBox.getMinY()),0),(long)Math.max(Math.floor(minBoundingBox.getMinZ()),0)},
				{Math.min((long)Math.ceil(minBoundingBox.getMaxX()), data.dimensions[0])-1,Math.min((long)Math.ceil(minBoundingBox.getMaxY()), data.dimensions[1])-1,Math.min((long)Math.ceil(minBoundingBox.getMaxZ()), data.dimensions[2])-1}};
		float[] block = new float[(int)((minMax[1][0]+1-minMax[0][0]) * (minMax[1][1]+1-minMax[0][1]) * (minMax[1][2]+1-minMax[0][2]))];

		tmp = Views.flatIterable(Views.interval(dataField, minMax[0], minMax[1]));

		int maxOcc =0;
		// copy values 
		int i = 0;
		float minValue = Float.MAX_VALUE;
		float maxValue = Float.MIN_VALUE; 
		Iterator<UnsignedShortType> values = (Iterator<UnsignedShortType>) tmp.iterator();
		for(long z = minMax[0][2]; z <=  minMax[1][2]; z++){
			for(long y = minMax[0][1]; y <= minMax[1][1]; y++){
				for(long x = minMax[0][0]; x <= minMax[1][0]; x++ ){
					short value = (short)values.next().get();

					block[i++] = value ;

					//update local distribution
					if(!distr.containsKey((float)value)){
						distr.put((float)value,0);
					}
					Integer curOcc= distr.get((float) value);
					curOcc++;
					distr.put((float)value,curOcc);
					maxOcc = Math.max(curOcc, maxOcc);
					minValue = Math.min(minValue, value);
					maxValue = Math.max(maxValue, value);
				}
			}
		}

		tmp.min(data.memOffset);
		for(int d = 0;d < 3 ;d++){
			data.memSize[d] = minMax[1][d]+1-minMax[0][d];
		}
		data.maxValue = maxValue;
		data.minValue = minValue;
		data.data = block;
		data.maxOccurance = maxOcc;
		AffineTransform3D sourceTransform3D = new AffineTransform3D();
		source.getSpimSource().getSourceTransform(currentTimePoint, midmap, sourceTransform3D);
		Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);
		data.setLocalTransformation(sourceTransformation);
		data.setNeedsUpdate(true);
		return data;
	}


	/**
	 * prints the voxel data as a paraview file
	 * @param data volume data array to write
	 * @param fileName file to write
	 */
	public static void writeParaviewFile(final VolumeDataBlock vData, final String fileName){
		PrintWriter paraWriter = null;
		List<List<Long>> cellDefines = new LinkedList<List<Long>>();
		List<Float> values = new ArrayList<Float>();
		try {
			paraWriter = new PrintWriter(fileName+".vtu","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//vti header
		paraWriter.println("# vtk DataFile Version 3.1");
		paraWriter.println("A converted sacked view");
		paraWriter.println("ASCII");
		paraWriter.println("DATASET UNSTRUCTURED_GRID");


		//write point data
		final int offsetNextNode = 1;
		final long offsetNextLine = vData.dimensions[0];
		final long offsetNextSlice = vData.dimensions[0]*vData.dimensions[1];
		int i =0;
		paraWriter.println("POINTS "+ vData.dimensions[0]*vData.dimensions[1]*vData.dimensions[2]+ " FLOAT" );
		for(int z = 0; z< vData.dimensions[2];z++){
			for(int y = 0; y < vData.dimensions[1]; y++){
				for (int x = 0; x < vData.dimensions[0]; x++){

					//not last element in dimension
					if(x < vData.dimensions[0]-1&&y < vData.dimensions[1]-1&&z < vData.dimensions[2]-1){
						Long currenVoxelID = (long)values.size();


						//add cell ids
						List<Long> cell = new ArrayList<Long>(8);
						cell.add(currenVoxelID);
						cell.add(currenVoxelID+offsetNextNode);
						cell.add(currenVoxelID+offsetNextLine);
						cell.add(currenVoxelID+offsetNextLine+offsetNextNode);
						cell.add(offsetNextSlice+currenVoxelID);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextNode);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextLine);
						cell.add(offsetNextSlice+currenVoxelID+offsetNextLine+offsetNextNode);

						cellDefines.add(cell);
					}

					String line = "" +x + " "+y+" "+z;
					values.add(vData.data[i++]);
					paraWriter.println(line);

				}
			}
		}
		paraWriter.println();
		//end points

		//write cells vti
		paraWriter.println("CELLS "+cellDefines.size()+" "+cellDefines.size() * 9);
		for(List<Long> cellDefine : cellDefines ){
			paraWriter.print(cellDefine.size());
			for(Long number: cellDefine){
				paraWriter.print(" "+number);
			}
			paraWriter.println();
		}
		paraWriter.println();

		//cell Type
		paraWriter.println("CELL_TYPES "+ cellDefines.size());
		for(i = 0; i < cellDefines.size(); i++ ){
			paraWriter.println(11);
		}
		paraWriter.println();
		//end cells

		//point data
		paraWriter.println("POINT_DATA "+ values.size() );
		paraWriter.println("SCALARS intensity FLOAT 1" );
		paraWriter.println("LOOKUP_TABLE default");
		for(Float s : values){
			paraWriter.println(s.toString());
		}
		//end point data

		paraWriter.close();
	}

	public static Color getColorOfVolume(int i){
		return volumeColor[i%volumeColor.length];
	}

	public static Matrix4 calcVolumeTransformation(final VolumeDataBlock block){
		Matrix4 trans = getNewIdentityMatrix();

		trans.multMatrix(block.getLocalTransformation());
		trans.scale(block.dimensions[0], block.dimensions[1], block.dimensions[2]);

		return trans;
	}

	public static Matrix4 fromCubeToNormalizedTextureSpace(final VolumeDataBlock block){
		Matrix4 trans= calcVolumeTransformation(block);
		//Matrix4 trans= copyMatrix(block.getLocalTransformation());
		trans.invert();
		
		Matrix4 tmp = getNewIdentityMatrix();
		//tmp.scale(block.dimensions[0], block.dimensions[1], block.dimensions[2]);
		tmp.multMatrix(trans);
		//long[] dim = block.dimensions;

		//	trans.scale((float)(dim[0]-1)/((float)dim[0]), (float)(dim[1]-1)/((float)dim[1]), (float)(dim[2]-1)/((float)dim[2]));
		//	trans.translate(1.f/(2.f*(float)dim[0]), 1.f/(2.f*(float)dim[1]), 1.f/(2.f*(float)dim[2]));
		return tmp;
		//return trans;
	}
	

	/**
	 * convolves the data in x and y dimension since these dimension have the highest resolution
	 * @param data
	 * @return
	 */
	public static LaplaceContainer calulateLablacianSimple(final VolumeDataBlock data){
		//assumeed kernel:
		//-1 -1 -1
		//-1  8 -1
		//-1 -1 -1
		float log2dKernel[][] = new float[][]{{-1,-1,-1},
											  {-1,8,-1},
											  {-1,-1,-1}};
		int xyz[] = new int[]{(int)data.memSize[0],(int)data.memSize[1],(int)data.memSize[2]};
		float convolved[] = new float[(int)(xyz[2]*xyz[1]*xyz[0])];
		
		LaplaceContainer ret = new LaplaceContainer();
		ret.dimension = xyz.clone();
		
		//folding
		for(int z = 0; z < xyz[2]; z++){
			int zOffset = z*xyz[0]*xyz[1];
			for(int y = 0; y < xyz[1]; y++){
				float kernelValue = 0f;
				int yOffset = y*xyz[0];
				int vertOffset = xyz[0];
				

				for(int x=0; x < xyz[0]; x++){
					//simple kernel evaluation
					kernelValue =0;
					for(int y1 = -1; y1 < 2; y1++){						
						for(int x1 = -1; x1 < 2; x1++){
							float dataValue;
							//repeat at border
							if((x == 0 && x1 ==-1) || (x == xyz[0]-1 && x1 ==1)||
									(y == 0 && y1 ==-1)|| (y == xyz[1]-1 && y1 ==1)){
								dataValue = data.data[zOffset + yOffset +x ];
							}else{
								dataValue = data.data[zOffset + yOffset  + vertOffset*y1 +x +x1];
							}
							float koeffizient;
							
							if(y1 == 0 && x1 == 0){
								koeffizient = 8;
							}else{
								koeffizient = -1;
							}
							kernelValue+= koeffizient * dataValue;
						}
					}
					ret.maxValue = Math.max(ret.maxValue, kernelValue);
					ret.minValue = Math.min(ret.minValue, kernelValue);
					convolved[zOffset+yOffset+x] = kernelValue;
				}
			}
		}
		ret.valueMesh3d = convolved;
		return ret;
	}
}
