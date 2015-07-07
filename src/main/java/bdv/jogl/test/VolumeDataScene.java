package bdv.jogl.test;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.read.ConvertedCursor;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;
import bdv.BigDataViewer;
import bdv.jogl.shader.UnitCube;
import bdv.viewer.state.SourceState;
import bdv.viewer.state.ViewerState;
import static bdv.jogl.test.MatrixUtils.*;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Matrix4;

/**
 * Describes a scene for volume data with multiple scene objects
 * @author michael
 *
 */
public class VolumeDataScene extends AbstractScene{

	private BigDataViewer bigDataViewer;

	private List<UnitCube> volumeBorders = new ArrayList<UnitCube>();

	@Override
	protected void disposeSpecial(GL2 gl2) {}


	@Override
	protected void resizeSpecial(GL2 gl2, int x, int y, int width, int height) {}


	public VolumeDataScene(BigDataViewer bdv){
		bigDataViewer = bdv;
	}


	/**
	 * initializes the scene once
	 * @param gl2
	 * @param width
	 * @param height
	 */
	protected void initSpecial(GL2 gl2, int width, int height){

		int numberOfSources = bigDataViewer.getViewer().getState().getSources().size();
		float colorLinearFactor = 1.f/numberOfSources;
		float r =0, g=1,b=1 ;
		for(int i = 0; i < bigDataViewer.getViewer().getState().getSources().size(); i++){

			UnitCube cubeShader = new UnitCube();
			volumeBorders.add(cubeShader);
			addSceneElement(cubeShader);
			cubeShader.init(gl2);
			cubeShader.setRenderWireframe(true);
			cubeShader.setColor(new Color(r,g,b,1));
			r+=colorLinearFactor;
			b-=colorLinearFactor;
		}
	}

	private short[][][] getDataBlock(RandomAccessibleInterval<?> source){
		IterableInterval<?> tmp = Views.flatIterable(source);
		Long maxX = tmp.dimension(0);
		Long maxY = tmp.dimension(1);
		Long maxZ = tmp.dimension(2);

		short[][][] block = new short[maxX.intValue()]
				[maxY.intValue()]
						[maxZ.intValue()];


		// copy values 
		Iterator<UnsignedShortType> values = (Iterator<UnsignedShortType>) tmp.iterator();
		for(int z = 0; z< maxZ.intValue(); z++){
			for(int y = 0; y < maxY.intValue(); y++){
				for(int x = 0; x < maxX.intValue(); x++ ){
					block[x][y][z] = (short)values.next().get() ;
				}
			}
		}
		return block;
	}


	/**
	 * prints the voxel data as a paraview file
	 * @param data
	 * @param fileName
	 */
	private static void toParaview(final short [][][] data, final String fileName){
		PrintWriter paraWriter = null;
		List<List<Integer>> cellDefines = new LinkedList<List<Integer>>();
		List<Short> values = new ArrayList<Short>();
		try {
			paraWriter = new PrintWriter(fileName+".vtu","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] dims = {data.length,data[0].length,data[0][0].length};

		//vti header
		paraWriter.println("# vtk DataFile Version 3.1");
		paraWriter.println("A converted sacked view");
		paraWriter.println("ASCII");
		paraWriter.println("DATASET UNSTRUCTURED_GRID");


		//write point data
		final int offsetNextNode = 1;
		final int offsetNextLine = dims[0];
		final int offsetNextSlice = dims[0]*dims[1];
		
		paraWriter.println("POINTS "+ dims[0]*dims[1]*dims[2]+ " FLOAT" );
		for(int z = 0; z< data[0][0].length;z++){
			for(int y = 0; y < data[0].length; y++){
				for (int x = 0; x < data.length; x++){

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
					values.add(data[x][y][z]);
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
		for(int i = 0; i < cellDefines.size(); i++ ){
			paraWriter.println(11);
		}
		paraWriter.println();
		//end cells

		//point data
		paraWriter.println("POINT_DATA "+ values.size() );
		paraWriter.println("SCALARS intensity FLOAT 1" );
		paraWriter.println("LOOKUP_TABLE default");
		for(Short s : values){
			paraWriter.println(s.toString());
		}
		//end point data
		paraWriter.close();
	}


	/**
	 * render the scene
	 * @param gl2
	 */
	protected void renderSpecial(GL2 gl2){

		AffineTransform3D viewerTransform = new AffineTransform3D();
		bigDataViewer.getViewer().getState().getViewerTransform(viewerTransform);

		ViewerState state = bigDataViewer.getViewer().getState();

		state.getViewerTransform(viewerTransform);
		List<SourceState<?>> sources = state.getSources();
		Matrix4 stateTrans = convertToJoglTransform(viewerTransform);

		int currentTimepoint = state.getCurrentTimepoint();
		int midMapLevel = 0;
		int i =0;
		for(SourceState<?> source : sources){
			Matrix4 mat = copyMatrix(stateTrans);

			RandomAccessibleInterval<?> ssource = source.getSpimSource().getSource(currentTimepoint, source.getSpimSource().getNumMipmapLevels()-1);

			//block transform
			AffineTransform3D sourceTransform3D = new AffineTransform3D();
			source.getSpimSource().getSourceTransform(currentTimepoint, midMapLevel, sourceTransform3D);
			Matrix4 sourceTransformation = convertToJoglTransform(sourceTransform3D);

		//		short[][][] values = getDataBlock(ssource);
		//	toParaview(values, "parafile");
		//	if(1==1)
		//	throw new NullPointerException();

			//block size
			long[] min =  new long[3];
			long[] dim =  new long[3];
			ssource.min(min);

			IterableInterval<?> tmp = Views.flatIterable(ssource);
			tmp.dimensions(dim);			

			Matrix4 scale = new Matrix4();
			scale.loadIdentity();
			scale.scale(dim[0], dim[1], dim[2]);

			mat.multMatrix(sourceTransformation);
			mat.multMatrix(scale);

			UnitCube cubeShader = volumeBorders.get(i);

			//mat.loadIdentity();
			cubeShader.setModelTransformations(mat);
			i++;
		}
	}
}
