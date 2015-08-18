package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * direct data manipulation panel
 * @author michael
 *
 */
public class TransferFunctionDataPanel extends JPanel {
	
	private TransferFunction1D transferFunction;
	
	private JScrollPane pointTableScroller;
	
	private JScrollPane colorTableScroller;
	
	private final JTable pointTable = new JTable(); 
	
	private final JTable colorTable = new JTable();
	
	private BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	
	private void initUI(){
		pointTableScroller.setPreferredSize(new Dimension(this.getWidth(),100));
		colorTableScroller.setPreferredSize(new Dimension(this.getWidth(),100));
		
		setLayout(mainLayout);		
		add(pointTableScroller);
		add(colorTableScroller);	
	}
	
	private void updateData(){
		
		updateFunctionPoints();
		
		updateColors();
	}
	
	private void updateFunctionPoints() {
		
		TreeSet<Point> functionPoints = transferFunction.getFunctionPoints();
		
		DefaultTableModel model = new DefaultTableModel(new String[]{"Transfer function points"},0);
		
		for(Point point: functionPoints){
			model.addRow(new Point[]{point});
		}
		pointTable.setModel(model);
		
	}

	private void updateColors() {
		
		TreeMap<Point, Color> colors = transferFunction.getColors();

		DefaultTableModel model =new DefaultTableModel(new String[]{"Color positions","colors"},0);
		
		for(Point position: colors.keySet()){
			Color color = colors.get(position);
			model.addRow(new Object[]{position,color});
			
			
		}
		
		colorTable.setModel(model);
	}

	public void setTransferFunction(TransferFunction1D tf){
		transferFunction = tf;
		transferFunction.addTransferFunctionListener(new TransferFunctionListener() {
			
			@Override
			public void colorChanged(TransferFunction1D transferFunction) {
				updateData();
				
			}
		});
	}
	
	public TransferFunctionDataPanel(final TransferFunction1D tf){
		
		pointTableScroller = new JScrollPane(pointTable);
		colorTableScroller = new JScrollPane(colorTable);
		initUI();
		
		setTransferFunction(tf);
	}
}
