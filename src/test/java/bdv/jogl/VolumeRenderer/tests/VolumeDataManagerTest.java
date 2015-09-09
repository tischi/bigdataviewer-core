package bdv.jogl.VolumeRenderer.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import bdv.jogl.VolumeRenderer.utils.VolumeDataBlock;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

public class VolumeDataManagerTest {

	private VolumeDataManager manager = new VolumeDataManager();
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
}
