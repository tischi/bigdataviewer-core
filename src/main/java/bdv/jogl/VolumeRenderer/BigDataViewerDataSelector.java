package bdv.jogl.VolumeRenderer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.geom.AABBox;

import bdv.BigDataViewer;
import static bdv.jogl.VolumeRenderer.utils.MatrixUtils.*;
import bdv.viewer.Source;
import bdv.viewer.state.SourceState;

public class BigDataViewerDataSelector {
	
	private final BigDataViewer bdv;
	
	public BigDataViewerDataSelector(final BigDataViewer bdv){
		this.bdv = bdv;
		
		initListener();
	}
	
	/**
	 * init all needed listeners
	 */
	private synchronized void initListener() {
	
		this.bdv.getViewerFrame().getViewerPanel().addMouseListener(new MouseAdapter() {
			
			@Override
			public synchronized void mouseClicked(MouseEvent e) {
				System.out.println("Hallo");
				
				//select on double click
				if(e.getClickCount() == 2){
					selectVolumePart(e.getPoint());
				}
			}
		});
	}
	
	/**
	 * event to select a appropriate part of the volumes
	 * @param p point on the panel
	 */
	public void selectVolumePart(final Point p){
		AABBox volumeRectangle = getVolumeRegion(bdv, p, new float[]{10,10,10});
		
		AABBox[] volumeCoords = getInnerVolumes(bdv, volumeRectangle, 0, bdv.getViewer().getState().getCurrentTimepoint());
		for(AABBox b : volumeCoords){
			System.out.println(b);
		}
	}
	
	/**
	 * get the AABB of the queried region
	 * @param bdv data source
	 * @param queryPoint center of the region
	 * @param dimensions size of the region (xyz) 
	 * @return AABB in global space
	 */
	private static AABBox getVolumeRegion(final BigDataViewer bdv, final Point queryPoint, float dimensions[] ){	
		AffineTransform3D transform3D = new AffineTransform3D();
		bdv.getViewer().getState().getViewerTransform(transform3D);
		Matrix4 transformJogl = convertToJoglTransform(transform3D);
		
		//get coord in global space
		float transformer[] = new float[]{queryPoint.x,queryPoint.y,0,1};
		float transformed[] = new float[4];
		transformJogl.multVec(transformer, transformed);
		
		//wclip
		for(int i = 0; i < 3; i++ ){
			transformed[i]/=transformed[3];
		}
	
		//build box
		AABBox box = new AABBox(transformed[0]-dimensions[0],transformed[1]-dimensions[1],transformed[2]-dimensions[2],
				transformed[0]+dimensions[0],transformed[1]+dimensions[1],transformed[2]+dimensions[2]);
		return box;
	}
	
	/**
	 * calculates the inner Volume borders of a certain region 
	 * @param bdv viewer
	 * @param outerVolume outer region
	 * @param midmapLevel current midmap to query
	 * @param time current time to query
	 * @return AABB of the volumes in volume coordinate system
	 */
	private static AABBox[] getInnerVolumes(final BigDataViewer bdv, final AABBox outerVolume, int midmapLevel, int time){
		//iterate sources
		List<SourceState<?>> sources = bdv.getViewer().getState().getSources();
		AABBox results[] = new AABBox[sources.size()];
		
		for(int n = 0; n < sources.size(); n++){
			Source<?> source = sources.get(n).getSpimSource();
			
			//get volume trans  
			AffineTransform3D sourceTrans3D = new AffineTransform3D();
			source.getSourceTransform(time, midmapLevel, sourceTrans3D);
			Matrix4 sourceTransJogl = convertToJoglTransform(sourceTrans3D);
			
			//test points for borders since they may overlap
			float[][]minMax={{Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE},
					{Float.MIN_VALUE,Float.MIN_VALUE,Float.MIN_VALUE}};
			float z = outerVolume.getMinZ();
			for(int i= 0; i < 2; i++ ){
				float y = outerVolume.getMinY();
				for(int j =0; j < 2; j++){
					float x =  outerVolume.getMinX();
					for(int k =0; k < 2; k++){
						float transformer[]={x,y,z,1};
						float transformed[] = new float[4];
						
						sourceTransJogl.multVec(transformer,transformed);
						
						//update min max
						for(int m = 0; m < 3; m++){
							minMax[0][m] = Math.min(minMax[0][m], transformed[m]/transformed[3]);
							minMax[1][m] = Math.max(minMax[0][m], transformed[m]/transformed[3]);
						}
						
						x = outerVolume.getMaxX();
					}
					y = outerVolume.getMaxY();
				}
				z = outerVolume.getMaxZ();
			}
			//add box
			results[n] = new AABBox(minMax[0], minMax[1]);
		}
		return results;
	}
	
}
