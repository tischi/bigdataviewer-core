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

import com.jogamp.opengl.math.Matrix4;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
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
	public static VolumeDataBlock getDataBlock(RandomAccessibleInterval<?> source){
		IterableInterval<?> tmp = Views.flatIterable(source);
		Long maxX = tmp.dimension(0);
		Long maxY = tmp.dimension(1);
		Long maxZ = tmp.dimension(2);

		float[] block = new float[maxX.intValue()*maxY.intValue()*maxZ.intValue()];

		VolumeDataBlock data = new VolumeDataBlock();
		TreeMap<Float, Integer> distr = data.getValueDistribution();
		int maxOcc =0;
		// copy values 
		int i = 0;
		float minValue = Float.MAX_VALUE;
		float maxValue = Float.MIN_VALUE; 
		Iterator<UnsignedShortType> values = (Iterator<UnsignedShortType>) tmp.iterator();
		for(int z = 0; z< maxZ.intValue(); z++){
			for(int y = 0; y < maxY.intValue(); y++){
				for(int x = 0; x < maxX.intValue(); x++ ){
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
		
		
		tmp.dimensions( data.dimensions);
		tmp.min(data.minPoint);
		tmp.max(data.maxPoint);
		data.maxValue = maxValue;
		data.minValue = minValue;
		data.data = block;
		data.maxOccurance = maxOcc;
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
		trans.invert();
		long[] dim = block.dimensions;
		trans.translate(1.f/(2.f*(float)dim[0]), 1.f/(2.f*(float)dim[1]), 1.f/(2.f*(float)dim[2]));
		trans.scale((float)(dim[0]-1)/((float)dim[0]), (float)(dim[1]-1)/((float)dim[1]), (float)(dim[2]-1)/((float)dim[2]));
	
		return trans;
	}
}
