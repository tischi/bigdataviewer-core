package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.utils.LaplaceContainer;
import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataUtils;

public class VolumeDataManagerTest {

	private VolumeDataManager manager = new VolumeDataManager(null);
	
	private float testData[][][] = new float[][][]{	{{1,1,1},{2,2,2},{3,3,3},{4,4,4}},
													{{1,1,1},{2,2,2},{3,3,3},{4,4,4}},
													{{1,1,1},{2,2,2},{3,3,3},{4,4,4}},
													{{1,1,1},{2,2,2},{3,3,3},{4,4,4}}};
	@Test
	public void dataAddingTest() {
		
	
		manager.setVolume(0,0,new VolumeDataBlock());
		assertEquals(1, manager.getVolumes().size());

		manager.setVolume(1,0,new VolumeDataBlock());
		assertEquals(2, manager.getVolumes().size());
		
		manager.removeVolumeByIndex(0);
		assertEquals(1, manager.getVolumes().size());
		assertEquals(null,manager.getVolume(0));
		assertNotEquals(null, manager.getVolume(1));
		
		manager.setVolume(0,0,new VolumeDataBlock());
		assertEquals(2, manager.getVolumes().size());
		assertNotEquals(null, manager.getVolume(0));
		assertNotEquals(null, manager.getVolume(1));
		
	}
	
	@Test
	public void simpleLaplaceTest(){
		VolumeDataBlock data = new VolumeDataBlock();
		data.memSize[2] = testData.length;
		data.memSize[1] = testData[0].length;
		data.memSize[0] = testData[0][0].length;
		data.data = new float[(int)(data.memSize[0]*data.memSize[1]*data.memSize[2])];
		
		for(int z = 0; z< data.memSize[2]; z++){
			for(int y = 0; y< data.memSize[1]; y++){
				for(int x = 0; x< data.memSize[0]; x++){
					data.data[(int)(z*data.memSize[0]*data.memSize[1]+y*data.memSize[0]+x)] = testData[z][y][x];
				}	
			}	
		}
		LaplaceContainer convolved = VolumeDataUtils.calulateLablacianSimple(data);
		assertEquals(data.data.length, convolved.valueMesh3d.length);
	}
}
