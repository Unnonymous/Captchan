import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
class CheckButton extends JToggleButton {
	
	//	private static final long serialVersionUID = 4414614513450750478L;
	private final JCheckBox checkBox = new JCheckBox();
	
	public CheckButton(String description) {
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(checkBox.isSelected())
					setSelected(true);
			}
		});
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkBox.isSelected() && !isSelected())
					checkBox.setSelected(false);
			}
		});
		checkBox.setContentAreaFilled(false);
		checkBox.setMargin(new Insets(0,getMargin().left,0,0));
		setLayout(null);
		setText(description);
		setHorizontalAlignment(SwingConstants.RIGHT);
		add(checkBox);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Dimension size = checkBox.getPreferredSize();
		int x = 0;//getMargin().left;
		int y = getHeight() / 2 - checkBox.getHeight() / 2;
		checkBox.setBounds(x,y,size.width,size.height);
		
		super.paintComponent(g);
	}
	
	public boolean isChecked() {
		return checkBox.isSelected();
	}
	
	public boolean isToggledOn() {
		return this.isSelected();
	}
	
	public void setToggledOn(boolean enabled) {
		setSelected(enabled);
		if(!enabled)
			checkBox.setSelected(enabled);
	}
}
