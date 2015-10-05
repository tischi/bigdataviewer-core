package bdv.jogl.VolumeRenderer.gui.TFDataPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;



public class PointCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer{

	/**
	 * default version
	 */
	private static final long serialVersionUID = 1L;

	private Point2D.Float currentPoint;

	private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.01));

	private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.01));

	private final JPanel editorPanel = new JPanel();

	private void buildUI(){
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
		editorPanel.add(new JLabel("x:"));
		editorPanel.add(xSpinner);
		xSpinner.setPreferredSize(new Dimension(250, xSpinner.getHeight()));
		ySpinner.setPreferredSize(new Dimension(250, ySpinner.getHeight()));
		editorPanel.add(new JLabel("y:"));
		editorPanel.add(ySpinner);
	};

	public PointCellEditor(){

		buildUI();

		createControls();
	}

	private void createControls() {
		xSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(currentPoint == null){
					return;
				}
				currentPoint.x =((Number)(xSpinner.getValue())).floatValue();
				fireEditingStopped();
			}
		});

		ySpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(currentPoint == null){
					return;
				}
				currentPoint.y = ((Number)(ySpinner.getValue())).floatValue();
				fireEditingStopped();
			}
		});
	}

	@Override
	public Point2D.Float getCellEditorValue() {
		return currentPoint;
	}


	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		currentPoint = (Point2D.Float)value;
		xSpinner.setValue(currentPoint.x);
		ySpinner.setValue(currentPoint.y);
		return editorPanel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object value, boolean arg2, boolean arg3, int arg4,
			int arg5) {  	       
		currentPoint = (Point2D.Float)value;
		xSpinner.setValue(currentPoint.x);
		ySpinner.setValue(currentPoint.y);  
		return editorPanel ;
	}

}
