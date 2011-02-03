import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JTextField;

@SuppressWarnings("serial")
class DescriptionField extends JTextField {
	
	//	private static final long serialVersionUID = -3740934612299923846L;
	private final String description;
	
	public DescriptionField(String description) {
		this.description = description;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(getText().equals("")) {
			g.setColor(Color.lightGray);
			int x = getInsets().left + 1;
			int y = getBaseline(getWidth(),getHeight());
			g.drawString(description,x,y);
		}
	}
}
