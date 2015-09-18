package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.jogl.VolumeRenderer.Scene.VolumeDataScene;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.IsoSurfaceVolumeInterpreter;
import bdv.jogl.VolumeRenderer.ShaderPrograms.MultiVolumeRenderer;
import bdv.jogl.VolumeRenderer.ShaderPrograms.ShaderSources.functions.TransparentVolumeinterpreter;
import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.PreIntegrationSampler;
import bdv.jogl.VolumeRenderer.TransferFunctions.sampler.RegularSampler;
import bdv.jogl.VolumeRenderer.gui.GLWindow.GLWindow;
import bdv.jogl.VolumeRenderer.gui.TFDataPanel.TransferFunctionDataPanel;
import bdv.jogl.VolumeRenderer.gui.TFDrawPanel.TransferFunctionDrawPanel;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.AggregatorManager;
import bdv.jogl.VolumeRenderer.gui.VDataAggregationPanel.VolumeDataAggregationPanel;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManager;
import bdv.jogl.VolumeRenderer.utils.VolumeDataManagerAdapter;

/**
 * Class for providing tf scene controls
 * @author michael
 *
 */
public class SceneControlsWindow extends JFrame {
	
	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private TransferFunctionDrawPanel tfpanel = null;
	
	private final JPanel mainPanel  = new JPanel();
	
	private TransferFunctionDataPanel tfDataPanel = null;
	
	private TransferFunction1D transferFunction;
	
	private VolumeDataAggregationPanel aggregationPanel;
	
	private JCheckBox usePreIntegration = new JCheckBox("Use pre-integration",false);
	
	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations",false);
	
	private JCheckBox showIsoSurface = new JCheckBox("Show iso surface", false);
	
	private JSpinner isoValueSpinner = new JSpinner();
	
	private JButton backgroundColorButton = new JButton("");
	
	private JPanel backgroundPanel = new JPanel();
	
	private final VolumeDataManager dataManager;
	
	private final JCheckBox rectBorderCheck = new JCheckBox("Show volume Borders",false);
	
	private final MultiVolumeRenderer renderer;
	
	private final VolumeDataScene scene;
	
	private final GLWindow drawWindow;
	
	private final JPanel isoPanel = new JPanel();
	
	private final JCheckBox showSlice = new JCheckBox("Show slice in 3D View");
	
	private final JPanel samplePanel = new JPanel();
	
	private final JSpinner sampleSpinner = new JSpinner(new SpinnerNumberModel(256, 1, 10000, 1));
	
	private final JCheckBox useGradient = new JCheckBox("Use gradients as values",false);
	
	public SceneControlsWindow(
			final TransferFunction1D tf,
			final AggregatorManager agm, 
			final VolumeDataManager dataManager, 
			final MultiVolumeRenderer mvr, 
			final GLWindow win,
			final VolumeDataScene scene){
		this.scene = scene;
		this.drawWindow = win;
		this.renderer = mvr;
		transferFunction = tf;
		this.dataManager = dataManager;
		createTFWindow(tf,agm,dataManager);
	}
	
	private void addComponetenToMainPanel(JComponent c){
		c.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(c);
	}
	
	private void createTFWindow(final TransferFunction1D tf,final AggregatorManager agm,final VolumeDataManager dataManager){
		tfpanel = new TransferFunctionDrawPanel(tf,dataManager);
		tfDataPanel = new TransferFunctionDataPanel(tf);
		aggregationPanel = new VolumeDataAggregationPanel(agm);


		setTitle("Transfer function configurations");
		setSize(640, 100);
		initAdvancedBox();
		initBackgroundPanel();
		initUsePreIntegration();
		initShowIsoSurface();
		initBorderCheck();
		initShowSlice();
		initSampleSpinner();
		initUseGradient();
	
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		addComponetenToMainPanel(tfpanel);
		addComponetenToMainPanel(advancedCheck);
		addComponetenToMainPanel(tfDataPanel);
		addComponetenToMainPanel(samplePanel);
		addComponetenToMainPanel(rectBorderCheck);
		addComponetenToMainPanel(showSlice);
		addComponetenToMainPanel(backgroundPanel);
		addComponetenToMainPanel(usePreIntegration);
		addComponetenToMainPanel(isoPanel);
		addComponetenToMainPanel(aggregationPanel);
		addComponetenToMainPanel(useGradient);
		
		tfDataPanel.setVisible(advancedCheck.isSelected());
		
		getContentPane().add(mainPanel);
		pack();
	}

	private void initSampleSpinner() {
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
		samplePanel.add(new JLabel("Render samples: "));
		samplePanel.add(sampleSpinner);
		
		updateSamples();
		sampleSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateSamples();	
			}
		});
	}

	private void updateSamples() {
		renderer.setSamples(((Number) sampleSpinner.getValue()).intValue());
		drawWindow.getGlCanvas().repaint();
	}

	private void initShowSlice() {
		updateSlice();
		showSlice.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				updateSlice();
			}
		});
		
	}

	private void updateSlice() {
		renderer.setSliceShown(showSlice.isSelected());
		drawWindow.getGlCanvas().repaint();
		
	}

	private void updateBorderStatus(){
		scene.enableVolumeBorders(rectBorderCheck.isSelected());
		drawWindow.getGlCanvas().repaint();
	}
	
	private void initBorderCheck() {
		updateBorderStatus();
		rectBorderCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateBorderStatus();
			}
		});
		
	}

	private void updateBackgroundColors(Color c){
		backgroundColorButton.setBackground(c);
		renderer.setBackgroundColor(c);
		drawWindow.getScene().setBackgroundColor(c);
		drawWindow.getGlCanvas().repaint();
	}
	
	private void initBackgroundPanel() {
		
		backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.X_AXIS));
		backgroundPanel.add(new JLabel("Background color: ") );
		backgroundPanel.add(backgroundColorButton);
		updateBackgroundColors( drawWindow.getScene().getBackgroundColor());
		
		backgroundColorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(new JFrame(), "color dialog", backgroundColorButton.getBackground());
				
				if(color == null && !drawWindow.getScene().getBackgroundColor().equals(color)){
					return;
				}
				
				updateBackgroundColors(color);
			}
		});
		
	}

	private void changeVolumeInterpreter(){
		if(showIsoSurface.isSelected()){
			renderer.getSource().setVolumeInterpreter(new IsoSurfaceVolumeInterpreter());
		}else{
			renderer.getSource().setVolumeInterpreter(new TransparentVolumeinterpreter());
		}
	}
	private void updateIsoSurface(){
		renderer.setIsoSurface(((Number)isoValueSpinner.getValue()).floatValue());
	}
	
	private void initShowIsoSurface() {
		dataManager.addVolumeDataManagerListener(new VolumeDataManagerAdapter() {
			
			@Override
			public void dataUpdated(Integer i) {
				float maxVolume=dataManager.getGlobalMaxVolumeValue();
				transferFunction.setMaxOrdinates(new Point2D.Float(maxVolume, 1.0f));
				isoValueSpinner.setModel(new SpinnerNumberModel(0.0,0.0, maxVolume, (maxVolume< 1.0)?0.1f:1.0f));
			}
			
			@Override
			public void dataEnabled(Integer i, Boolean flag) {
				drawWindow.getGlCanvas().repaint();
			}
		});	
		
		changeVolumeInterpreter();
		showIsoSurface.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeVolumeInterpreter();
				drawWindow.getGlCanvas().repaint();
			}
		});
		
		updateIsoSurface();
		isoValueSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateIsoSurface();
				drawWindow.getGlCanvas().repaint();
			}
		});
		
		isoPanel.setLayout(new BoxLayout(isoPanel, BoxLayout.X_AXIS));
		isoPanel.add(showIsoSurface);
		isoPanel.add(isoValueSpinner);
		
	}

	private void changeTransferfuntionSampler(){
		if(usePreIntegration.isSelected()){
			transferFunction.setSampler(new PreIntegrationSampler());
		}else{
			transferFunction.setSampler(new RegularSampler());
		}
	}
	
	private void initUsePreIntegration() {
		changeTransferfuntionSampler();
		usePreIntegration.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				changeTransferfuntionSampler();
			}
		});
	}

	private void initAdvancedBox() {
		advancedCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				tfDataPanel.setVisible(advancedCheck.isSelected());
				pack();
				
			}
		});
		
	}

	public void destroyTFWindow() {
		dispose();
		tfpanel = null;
	}
}
