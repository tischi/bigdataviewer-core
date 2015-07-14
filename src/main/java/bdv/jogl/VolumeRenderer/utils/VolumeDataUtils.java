package bdv.jogl.VolumeRenderer.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
					minValue = Math.min(minValue, value);
					maxValue = Math.max(maxValue, value);
				}
			}
		}
		
		VolumeDataBlock data = new VolumeDataBlock();
		tmp.dimensions( data.dimensions);
		data.maxValue = maxValue;
		data.minValue = minValue;
		data.data = block;
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

}
