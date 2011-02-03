import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

class ReCaptchaWindow {
	final JDialog reFrame = new JDialog();
	final JPanel rePanel = new JPanel();
	final JLabel reImage = new JLabel();
	final DescriptionField reInput = new DescriptionField("(New captcha)");
	final JButton reBreak = new JButton("Break");
	final WindowAdapter actBreak;
	String reChal;
	
	public ReCaptchaWindow(WindowAdapter actBreak) {
		this.actBreak = actBreak;
	}
	
	void setup() {
		addComponents();
		addListeners();
	}
	
	private void addComponents() {
		newChallenge();
		
		reInput.setColumns(20);
		
		rePanel.add(reInput);
		rePanel.add(reBreak);
		
		reFrame.add(reImage,BorderLayout.NORTH);
		reFrame.add(rePanel,BorderLayout.SOUTH);
		
		reFrame.setLocationRelativeTo(null);
		reFrame.setResizable(false);
		reFrame.setModal(true);
		reFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		reFrame.pack();
	}
	
	private void addListeners() {
		reFrame.addWindowListener(actBreak);
	}
	
	void newChallenge() {
		reChal = getCaptchaChallenge();
		ImageIcon image = null;
		try {
			image = new ImageIcon(new URL("http://www.google.com/recaptcha/api/image?c="
			    + reChal));
		} catch(MalformedURLException e) {
			System.err.println("Captcha image URL is malformed and can't be loaded");
		}
		reImage.setIcon(image);
		if(image != null) {
			reImage.setSize(image.getIconWidth(),image.getIconHeight());
		}
	}
	
	private static String getCaptchaChallenge() {
		String page = null;
		try {
			page = ClientHttpRequest
			    .get(new URL(
			        "http://www.google.com/recaptcha/api/noscript?k=6Ldp2bsSAAAAAAJ5uyx_lx34lJeEpTLVkP5k04qc"));
		} catch(MalformedURLException e) {
			System.err.println("Captcha page URL is malformed and can't be loaded");
		}
		
		if(page != null) {
			int left = page.indexOf("image?c=") + "image?c=".length();
			int right = page.indexOf('"',left);
			return page.substring(left,right);
		}
		return null;
	}
	
}
