package bdv.jogl.VolumeRenderer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.imglib2.ui.TransformListener;
import bdv.BigDataViewer;
import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AbstractVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.AverageVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaxDifferenceAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MaximumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.accumulator.MinimumVolumeAccumulator;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunctionAdapter;
import bdv.jogl.VolumeRenderer.gui.SceneControlsWindow;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.AggregatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.IVolumeAggregationListener;
import bdv.jogl.VolumeRenderer.gui.VolumeRendereActions.OpenVolumeRendererAction;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;

/**
 * The context class of the bdv 3D volume extension
 * @author michael
 *
 */
public class VolumeRendererExtension {

	private final BigDataViewer bdv;
	
	private final GLWindow glWindow;
	
	private final VolumeDataManager dataManager = new VolumeDataManager(); 
	
	private final VolumeDataScene dataScene;
	
	private final static String preferedMenuName = "Tools";

	private final static String actionName = "3D Volume";

	private final MultiVolumeRenderer volumeRenderer;
	
	private final TransferFunction1D transferFunction = new TransferFunction1D();

	private SceneControlsWindow controls;
	
	private void createControlWindow(){
		//window
		Set<AbstractVolumeAccumulator> acc =  new HashSet<AbstractVolumeAccumulator>();
		AverageVolumeAccumulator avg = new AverageVolumeAccumulator();
		acc.add(avg);
		acc.add(new MaximumVolumeAccumulator());
		acc.add(new MinimumVolumeAccumulator());
		acc.add(new MaxDifferenceAccumulator());
		AggregatorManager aggm = new AggregatorManager(acc);
		aggm.setActiveAcumulator(avg.getFunctionName());
		aggm.addListener(new IVolumeAggregationListener() {
			
			@Override
			public void aggregationChanged(AbstractVolumeAccumulator acc) {
				volumeRenderer.getSource().setAccumulator(acc);
				glWindow.getGlCanvas().repaint();
			}
		});
		controls =new SceneControlsWindow(transferFunction,aggm, dataManager);
	}
	
	public VolumeRendererExtension(final BigDataViewer bdv){
		if(bdv == null){
			throw new NullPointerException("The extension needs a valid big data viewer instance");
		}
		
		this.bdv = bdv;
		
		
		
		volumeRenderer = new MultiVolumeRenderer(transferFunction, dataManager);
		

		dataScene = new VolumeDataScene(bdv, dataManager,volumeRenderer);
		BigDataViewerAdapter.connect(this.bdv, dataManager);
		this.bdv.getViewer().addTransformListener(new SceneGlobalTransformationListener(dataScene));
		glWindow = new GLWindow(dataScene);
		createControlWindow();
		createAndConnect3DView(this.bdv);

		transferFunction.addTransferFunctionListener( new TransferFunctionAdapter() {

			@Override
			public void colorChanged(final TransferFunction1D function) {

				//trigger scene update
				glWindow.getGlCanvas().repaint();
			}
			
			@Override
			public void samplerChanged(TransferFunction1D transferFunction1D) {
				glWindow.getGlCanvas().repaint();
			}
		});
	}
	
	/**
	 * creates a GLWidget and connects it to the viewer 
	 * @param parent The BigDataViewer to connect to
	 * @return The new created GLWidget
	 */
	private void createAndConnect3DView(BigDataViewer parent){
		JMenuBar menuBar = bdv.getViewerFrame().getJMenuBar();


		JMenu preferedMenu = null;

		//find Tools menu
		for(int i = 0; i < menuBar.getMenuCount(); i++){
			JMenu currentMenu = menuBar.getMenu(i);
			if(currentMenu.getText().equals(preferedMenuName)){
				preferedMenu = currentMenu;
			}
		}

		//create if not exists
		if(preferedMenu == null){
			preferedMenu = new JMenu(preferedMenuName);
		}

		//close listener
		this.bdv.getViewerFrame().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				glWindow.dispose();
				controls.dispose();
			}
		});
		Action open3DViewAction = new OpenVolumeRendererAction(actionName, glWindow, controls);
		preferedMenu.add(open3DViewAction);
		preferedMenu.updateUI();

	}
	
	/**
	 * clears the context
	 */
	public void delete(){
		glWindow.dispose();
		controls.destroyTFWindow();
	}
}
