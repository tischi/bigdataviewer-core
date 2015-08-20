package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.EventObject;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import bdv.jogl.VolumeRenderer.TransferFunctions.TransferFunction1D;

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
		
		final TreeSet<Point> functionPoints = transferFunction.getFunctionPoints();
		
		DefaultTableModel model = new DefaultTableModel(new String[]{"Transfer function points"},0);
		
		for(Point point: functionPoints){
			model.addRow(new Point[]{point});
		}
		pointTable.setModel(model);
		model.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				if(e.getType() == TableModelEvent.UPDATE){
					//points changed TODO
					if(e.getColumn() == 0){
						Point[] newPoints = new Point[functionPoints.size()];
						Point[] oldPoints = new Point[functionPoints.size()];
						
						functionPoints.toArray(newPoints);
						transferFunction.getFunctionPoints().toArray(oldPoints);
						transferFunction.updateFunctionPoint(oldPoints[e.getFirstRow()],newPoints[e.getFirstRow()]);
					}
				}
			}
		});
		
		PointCellEditor pointCellEditor  = new PointCellEditor();
		pointTable.getColumnModel().getColumn(0).setCellEditor(pointCellEditor);
		pointTable.getColumnModel().getColumn(0).setCellRenderer(pointCellEditor);
		
	}

	private void updateColors() {
		
		final TreeMap<Point, Color> colors = transferFunction.getColors();

		final DefaultTableModel model =new DefaultTableModel(new String[]{"Color position","Colors"},0);
		for(Point position: colors.keySet()){
			Color color = colors.get(position);
			model.addRow(new Object[]{position,color});
			
		}

		colorTable.setModel(model);
		model .addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				
				if(e.getType() == TableModelEvent.UPDATE){

					//color changed
					if(e.getColumn() == 1){
						int row = e.getFirstRow();
						Point colorPosition = (Point) model.getValueAt(row, 0);
						Color newColor = (Color) model.getValueAt(e.getFirstRow(), 1); 
						transferFunction.setColor(colorPosition, newColor);
						
					}
					
					//points changed TODO
					if(e.getColumn() == 0){
						Point[] newPoints = new Point[colors.size()];
						Point[] oldPoints = new Point[colors.size()];
						
						colors.keySet().toArray(newPoints);
						transferFunction.getColors().keySet().toArray(oldPoints);
						transferFunction.moveColor(oldPoints[e.getFirstRow()],newPoints[e.getFirstRow()]);
					}
				}
			}
		});
		
		ColorCellEditor colorEditor = new ColorCellEditor();		
		PointCellEditor pointEditor = new PointCellEditor();

		colorTable.getColumnModel().getColumn(0).setCellEditor(pointEditor);
		colorTable.getColumnModel().getColumn(0).setCellRenderer(pointEditor);
		
		//add button
		colorTable.getColumnModel().getColumn(1).setCellEditor(colorEditor);
		colorTable.getColumnModel().getColumn(1).setCellRenderer(colorEditor);
		
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
