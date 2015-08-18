package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.TableView;

/**
 * direct data manipulation panel
 * @author michael
 *
 */
public class TransferFunctionDataPanel extends JPanel {
	
	private JScrollPane pointTableScroller;
	
	private JScrollPane colorTableScroller;
	
	private ItemListener advanceListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent arg0) {
			pointTableScroller.setVisible(advancedCheck.isSelected());
			colorTableScroller.setVisible(advancedCheck.isSelected());			
		}
	};
	
	private JCheckBox advancedCheck = new JCheckBox("Advanced configurations");
	
	private DefaultTableModel model = new DefaultTableModel(new Object[][]{{true}},  new String[]{"Transfer function points"});
	
	private JTable pointTable = new JTable(model); 
	
	private JTable colorTable = new JTable(new Object[][]{{new Point(0,0),Color.red}}, new String[]{"Positions","Color"});
	
	private BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	


	private void initCheckBox(){

		advancedCheck.addItemListener(advanceListener);
		advancedCheck.setSelected(false);
		
		//update all events
		advanceListener.itemStateChanged(null);

	}
	
	private void initUI(){
		setLayout(mainLayout);		
		add(advancedCheck);
		add(pointTableScroller);
		add(colorTableScroller);	
	}
	
	public TransferFunctionDataPanel(){
		pointTableScroller = new JScrollPane(pointTable);
		colorTableScroller = new JScrollPane(colorTable);
		initCheckBox();
		initUI();
	}
}
