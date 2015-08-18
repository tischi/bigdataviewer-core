package bdv.jogl.VolumeRenderer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
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

		private Point currentPoint;
		
		private final JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		
		private final JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		
		private final JPanel editorPanel = new JPanel();
		
		private void buildUI(){
			editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
			editorPanel.add(new JLabel("x:"));
			editorPanel.add(xSpinner);
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
					currentPoint.x = (Integer)xSpinner.getValue();
					fireEditingStopped();
				}
			});
			
			ySpinner.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					if(currentPoint == null){
						return;
					}
					currentPoint.y = (Integer)ySpinner.getValue();
					fireEditingStopped();
				}
			});
		}

		@Override
		public Point getCellEditorValue() {
			return currentPoint;
		}

		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	        currentPoint = (Point)value;
	        xSpinner.setValue(currentPoint.x);
	        ySpinner.setValue(currentPoint.y);
	        return editorPanel;
		}

		@Override
		public Component getTableCellRendererComponent(JTable arg0, Object value, boolean arg2, boolean arg3, int arg4,
				int arg5) {  	       
			Point tmpPoint = (Point)value;
		    xSpinner.setValue(tmpPoint.x);
		    ySpinner.setValue(tmpPoint.y);  
			return editorPanel ;
		}

	}
