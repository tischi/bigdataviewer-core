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
	public static float[] getDataBlock(RandomAccessibleInterval<?> source){
		IterableInterval<?> tmp = Views.flatIterable(source);
		Long maxX = tmp.dimension(0);
		Long maxY = tmp.dimension(1);
		Long maxZ = tmp.dimension(2);

		float[] block = new float[maxX.intValue()*maxY.intValue()*maxZ.intValue()];


		// copy values 
		int i = 0;
		Iterator<UnsignedShortType> values = (Iterator<UnsignedShortType>) tmp.iterator();
		for(int z = 0; z< maxZ.intValue(); z++){
			for(int y = 0; y < maxY.intValue(); y++){
				for(int x = 0; x < maxX.intValue(); x++ ){
					block[i++] = (float)values.next().get() ;
				}
			}
		}
		return block;
	}


	/**
	 * prints the voxel data as a paraview file
	 * @param data volume data array to write
	 * @param fileName file to write
	 */
	public static void writeParaviewFile(final float [] data,final int[] dims, final String fileName){
		PrintWriter paraWriter = null;
		List<List<Integer>> cellDefines = new LinkedList<List<Integer>>();
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
		final int offsetNextLine = dims[0];
		final int offsetNextSlice = dims[0]*dims[1];
		int i =0;
		paraWriter.println("POINTS "+ dims[0]*dims[1]*dims[2]+ " FLOAT" );
		for(int z = 0; z< dims[2];z++){
			for(int y = 0; y < dims[1]; y++){
				for (int x = 0; x < dims[0]; x++){

					//not last element in dimension
					if(x < dims[0]-1&&y < dims[1]-1&&z < dims[2]-1){
						int currenVoxelID = values.size();
						
						
						//add cell ids
						List<Integer> cell = new ArrayList<Integer>(8);
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
					values.add(data[i++]);
					paraWriter.println(line);

				}
			}
		}
		paraWriter.println();
		//end points

		//write cells vti
		paraWriter.println("CELLS "+cellDefines.size()+" "+cellDefines.size() * 9);
		for(List<Integer> cellDefine : cellDefines ){
			paraWriter.print(cellDefine.size());
			for(Integer number: cellDefine){
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
