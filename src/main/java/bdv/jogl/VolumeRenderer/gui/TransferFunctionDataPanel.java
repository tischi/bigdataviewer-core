package bdv.jogl.VolumeRenderer.gui;

import java.awt.Color;
import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.text.TableView;

/**
 * direct data manipulation panel
 * @author michael
 *
 */
public class TransferFunctionDataPanel extends JPanel {
	
	private JTable pointTable = new JTable(new Object[][]{{new Point(0,0)}}, new String[]{"Transfer function points"}); 
	
	private JTable colorTable = new JTable(new Object[][]{{new Point(0,0),Color.red}}, new String[]{"Positions","Color"});
	
	private BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	
	public TransferFunctionDataPanel(){
		
		setLayout(mainLayout);
		
		add(pointTable);
		
		add(colorTable);
	}
}
