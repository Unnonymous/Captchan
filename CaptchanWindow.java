import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

class CaptchanWindow {
	final JFrame frame = new JFrame("Captchan");
	final JPanel panel = new JPanel();
	final JButton btnSelect = new JButton("Select Files...");
	final DescriptionField tfBoard = new DescriptionField("Board");
	final DescriptionField tfThread = new DescriptionField("Thread");
	final DescriptionField tfName = new DescriptionField("Name");
	final DescriptionField tfEmail = new DescriptionField("Email");
	final DescriptionField tfSubject = new DescriptionField("Subject");
	final DescriptionField tfComment = new DescriptionField("Comment");
	final DescriptionField tfSeconds = new DescriptionField("Interval");
	final JButton btnAction = new JButton("Start");
	final JTextArea taLog = new JTextArea("Init");
	final JTextArea taMsg = new JTextArea();
	private final JScrollPane taLogScroll = new JScrollPane(taLog);
	private final JScrollPane taMsgScroll = new JScrollPane(taMsg);
	final CheckButton btnSend = new CheckButton("Queue Msg");
	
	//for some reason, this string is smaller than the equivalent byte array.
	final Image icon = new ImageIcon(new java.math.BigInteger(
	    "-1o0vzz3m49w1ii1o57cclx6cuau8s4f06il9jt5esool6amf5y"
	        + "7yfnfvxlnonv964mgvvp3qqe5ygsx0nmn9tvzu4sxiskxf97dea"
	        + "4hcuynrzk0zca1ii5of60iebuakfvm0yj2kuav75fijkcflltbu"
	        + "rw9mxhi9hiopoqd8yd856zifwb1z131t2bzpm473as44vd8oo7q"
	        + "ulrgbcsso12zs0d9pfzcvqn8elmm8feubf25jfsev2nwc9jzdy9"
	        + "0mgv12b4shiuepwynit4f34ae8m5rn65oe2qcuhnqnl4qngk1uc"
	        + "7xxys1ugsdsobbctw6sgffdi60xkpb73wmdmjssai4zk34fpmm",36).toByteArray())
	    .getImage();
	
	public CaptchanWindow() {
	}
	
	private void addPanelComponents() {
		panel.setLayout(new GridLayout(9,1,0,2));
		panel.add(btnSelect);
		panel.add(tfBoard);
		panel.add(tfThread);
		panel.add(tfName);
		panel.add(tfEmail);
		panel.add(tfSubject);
		panel.add(tfComment);
		panel.add(tfSeconds);
		panel.add(btnAction);
	}
	
	private void addFrameComponents() {
		GridSpanLayout gsl = new GridSpanLayout(9,2,2,1);
		gsl.setSpan(0,0,9,1);
		gsl.setSpan(0,1,5,1);
		gsl.setSpan(6,1,3,1);
		
		frame.setLayout(gsl);
		frame.add(panel);
		frame.add(taLogScroll);
		frame.add(btnSend);
		frame.add(taMsgScroll);
	}
	
	private void setUpTextAreas() {
		Dimension textAreaSize = new Dimension(107,50);
		
		taLogScroll.setPreferredSize(textAreaSize);
		taLogScroll
		    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		taLogScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		taMsgScroll.setPreferredSize(textAreaSize);
		taMsgScroll
		    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		taMsgScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		Font font = new Font("Sans-serif",Font.PLAIN,9);
		
		taLog.setForeground(Color.gray);
		taLog.setEditable(false);
		taLog.setFont(font);
		taLog.setLineWrap(true);
		
		taMsg.setFont(font);
		taMsg.setLineWrap(true);
	}
	
	private void setUpFrame() {
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setIconImage(icon);
	}
	
	void setComponentsEnabled(boolean enabled) {
		Component components[] = panel.getComponents();
		for(Component component:components)
			component.setEnabled(enabled);
	}
	
	void log(String str) {
		// Autoscroll does not function for some reason.
		//		javax.swing.JScrollBar vbar = logScroller.getVerticalScrollBar();
		//		javax.swing.BoundedRangeModel model = vbar.getModel();
		//		model.getExtent();
		//		int max = model.getMaximum() - model.getExtent();
		//		int val = model.getValue();
		//		boolean autoScroll = (max == val);
		//		// getMaximum - max - extent
		//		
		//		//		boolean autoScroll = ((vbar.getValue() + vbar.getVisibleAmount()) == vbar
		//		//				.getMaximum());
		taLog.append("\n" + str);
		//		if(autoScroll) {
		taLog.setCaretPosition(taLog.getDocument().getLength());
		//		}
	}
	
	void setup() {
		addPanelComponents();
		addFrameComponents();
		setUpTextAreas();
		setUpFrame();
	}
	
}
