import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class Captchan {
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(Exception e) {
					System.err.println("Unable to load native look and feel");
				}
				new Captchan();
			}
		});
	}
	
	private String MAX_FILE_SIZE;
	private String topicURL;
	private String postURL;
	private String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; "
	    + "rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 GTB7.1";
	
	private final Deque<String> reChallenges = new LinkedList<String>();
	private final Deque<String> reResponses = new LinkedList<String>();
	private File files[];
	private int workingOn;
	
	// matches ${n} and ${n+x}, x is captured
	private final Pattern pattern = Pattern.compile("\\$\\{n(?:\\+(\\d*))?\\}");
	
	private final Object lock = new Object();
	private volatile boolean isBroken;
	private volatile boolean isPaused;
	private boolean isStarted;
	private boolean isTextSent;
	
	private final CaptchanWindow captchan = new CaptchanWindow();
	private final ReCaptchaWindow reCaptcha = new ReCaptchaWindow(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			stopEnteringCaptchas();
		}
	});
	
	private final WindowAdapter actConfirmStop = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			int returnValue = JOptionPane.showConfirmDialog(captchan.frame,"Stop?",
			    "Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
			if(returnValue == JOptionPane.YES_OPTION) {
				isBroken = true;
				isPaused = false;
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		}
	};
	
	private Captchan() {
		setupComponents();
		loadSettings();
		addListeners();
		useFastCloseListener();
		initialLog();
		
		captchan.frame.setVisible(true);
	}
	
	private void setupComponents() {
		captchan.setup();
		reCaptcha.setup();
	}
	
	private void addListeners() {
		captchan.btnSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				java.awt.FileDialog fd = new java.awt.FileDialog(captchan.frame,
				    "Select multiple files",java.awt.FileDialog.LOAD);
				fd.setIconImage(captchan.icon);
				fd.setMultipleMode(true);
				fd.setFilenameFilter(new java.io.FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						// Open only images
						if(dir.isDirectory()) {
							return true;
						}
						name = name.toLowerCase();
						if(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")
						    || name.endsWith(".png") || name.endsWith(".bmp")) {
							return true;
						}
						return false;
						// throw new UnsupportedOperationException("Not supported yet.");
					}
				});
				fd.setVisible(true);
				
				File temp[] = fd.getFiles();
				if(temp.length > 0) {
					files = temp;
					captchan.btnSelect.setText(Integer.toString(files.length) + " Selected...");
				}
			}
			
			//			@Override
			//			public void actionPerformed(ActionEvent e) {
			//				final java.awt.Frame f = new java.awt.Frame();
			//				f.setIconImage(icon);
			//				final javax.swing.JFileChooser filechooser = new javax.swing.JFileChooser();
			//				filechooser.setMultiSelectionEnabled(true);
			//				javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
			//					
			//					@Override
			//					public boolean accept(File pathname) {
			//						if(pathname.isDirectory()) {
			//							return true;
			//						}
			//						
			//						String ext = pathname.getName().toLowerCase();
			//						return ext.endsWith("jpg") || ext.endsWith("jpeg") || ext.endsWith("gif")
			//						    || ext.endsWith("png") || ext.endsWith("bmp");
			//					}
			//					
			//					@Override
			//					public String getDescription() {
			//						return "Images";
			//					}
			//				};
			//				filechooser.setFileFilter(filter);
			//				int returnval = filechooser.showOpenDialog(f);
			//				if(returnval == javax.swing.JFileChooser.APPROVE_OPTION) {
			//					files = filechooser.getSelectedFiles();
			//					captchan.btnSelect.setText(Integer.toString(files.length) + " Selected...");
			//				}
			//			}
		});
		captchan.btnAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(isStarted) {
					pauseResume();
				} else {
					start();
				}
			}
			
			private void start() {
				boolean isContent = (files != null) && (files.length > 0);
				boolean isBoardChosen = (!captchan.tfBoard.getText().equals(""));
				if(isContent && isBoardChosen) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							work();
						}
					}).start();
				}
			}
			
			private void pauseResume() {
				synchronized(lock) {
					isPaused = !isPaused;
					captchan.btnAction.setText(isPaused?"Resume":"Pause");
					lock.notifyAll();
				}
			}
		});
		reCaptcha.reBreak.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopEnteringCaptchas();
			}
		});
		reCaptcha.reInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isNewChallengeRequested = (reCaptcha.reInput.getText().equals(""));
				if(isNewChallengeRequested) {
					reCaptcha.newChallenge();
				} else {
					reChallenges.addFirst(reCaptcha.reChal);
					reResponses.addFirst(reCaptcha.reInput.getText());
					
					reCaptcha.reInput.setText("");
					reCaptcha.newChallenge();
					
					boolean isAnotherChallenge = (reChallenges.size() + workingOn < files.length);
					if(isAnotherChallenge) {
						solveCaptchas();
					} else {
						resetTitles();
					}
				}
			}
		});
	}
	
	private void stopEnteringCaptchas() {
		reCaptcha.reInput.requestFocus();
		isBroken = true;
		resetTitles();
	}
	
	private void initialLog() {
		log("* Thread empty = \n   new thread");
		log("* ${n} in comment = \n   # being uploaded");
		log("* Queue Msg sends \n   the text below in \n   the next post once");
		log("* Checkbox keeps \n   button pressed");
	}
	
	private void loadSettings() {
		try {
			Scanner scan = new Scanner(new File("captchan.ini"));
			System.out.println("Loading settings");
			String[] settings = {"board=","name=","email=","subject=","comment=","interval="};
			DescriptionField[] boxes = {captchan.tfBoard,captchan.tfName,captchan.tfEmail,
			    captchan.tfSubject,captchan.tfComment,captchan.tfSeconds};
			while(scan.hasNextLine()) {
				String input = scan.nextLine();
				for(int i = 0;i < settings.length;i++) {
					boolean isLineContentSetting = (input.toLowerCase().startsWith(settings[i]));
					boolean isLineUserAgent = (input.toLowerCase().startsWith("user-ugent="));
					if(isLineContentSetting) {
						boxes[i].setText(input.substring(settings[i].length()));
						break;
					} else if(isLineUserAgent) {
						userAgent = input.substring("user-agent".length());
						break;
					}
				}
			}
			scan.close();
		} catch(FileNotFoundException e) {
			// Don't load default settings
		}
	}
	
	private void log(String str) {
		captchan.log(str);
	}
	
	private void work() {
		firstTasks();
		
		while(workingOn < files.length) {
			allowPause();
			
			if(isBroken) {
				// If the user broke the process, exit
				updateFiles(workingOn);
				break;
			}
			
			String result;
			try {
				log("Uploading " + (workingOn + 1));
				log(":" + files[workingOn].getName());
				
				result = upload();
			} catch(UploadFailException e) {
				log("Upload failed");
				continue;
			} catch(BadCaptchaException e) {
				log("Mistyped captcha");
				reCaptcha.newChallenge();
				solveCaptchas();
				continue;
			} catch(FileTooLargeException e) {
				log("File too large; skipping");
				log(": File size is " + files[workingOn].length());
				workingOn++;
				continue;
			} catch(MaliciousFileException e) {
				log("Malicious file");
				continue;
			} catch(FloodException e) {
				log("Flood detected");
				int interval = Integer.parseInt(captchan.tfSeconds.getText());
				interval += interval / 2;
				captchan.tfSeconds.setText(Integer.toString(interval));
				sleep();
				continue;
			} catch(MaxLimitException e) {
				log("Max Limit");
				isBroken = true;
				continue;
			} catch(Error404Exception e) {
				log("404");
				isBroken = true;
				continue;
			} catch(UploadException e) {
				log("Error");
				System.err.println("Unknown upload error");
				isBroken = true;
				continue;
			} catch(IOException e) {
				log("Error posting");
				isBroken = true;
				continue;
			}
			
			boolean isFirstFile = (workingOn == 0);
			boolean isThreadSpecified = (!captchan.tfThread.getText().equals(""));
			if(isFirstFile && !isThreadSpecified)
				getResourceThread(result);
			
			clearCommentArea();
			log("Success");
			
			boolean isLastFile = (workingOn == files.length - 1);
			if(isLastFile) {
				log("Complete");
				break; // don't bother sleeping for the last file
			}
			
			sleep();
			
			workingOn++; // If successful, work on the next file
		}
		done();
	}
	
	private void firstTasks() {
		log("-----");
		log("Start");
		
		initVariables();
		
		log("Max file size: " + MAX_FILE_SIZE);
	}
	
	private void initVariables() {
		isStarted = true;
		isPaused = false;
		isBroken = false;
		isTextSent = false;
		workingOn = 0;
		
		captchan.btnAction.setText("Pause");
		
		captchan.setComponentsEnabled(false);
		useConfirmStopListener();
		
		String board = captchan.tfBoard.getText();
		String thread = captchan.tfThread.getText();
		
		topicURL = "http://boards.4chan.org/" + board;
		postURL = "http://sys.4chan.org/" + board + "/post";
		boolean isThreadSpecified = (!thread.equals(""));
		if(isThreadSpecified)
			topicURL += "/res/" + thread;
		
		boolean isIntervalSpecified = (!captchan.tfSeconds.getText().equals(""));
		if(!isIntervalSpecified)
			captchan.tfSeconds.setText("60");
		
		MAX_FILE_SIZE = ChanBoard.getMaxFileSize(topicURL);
		
		solveCaptchas();
		
		// After solving captchas, allow pausing
		captchan.btnAction.setEnabled(true);
	}
	
	private void solveCaptchas() {
		boolean isAnotherChallenge = (reChallenges.size() + workingOn < files.length);
		if(isAnotherChallenge) {
			captchan.frame.setTitle("*Captchan");
			boolean isFirstCaptcha = (workingOn == 0);
			if(isFirstCaptcha) // If initializing captchas, show progress
				reCaptcha.reFrame.setTitle("Enter Captcha (" + (1 + reChallenges.size()) + "/"
				    + files.length + ")");
			reCaptcha.reFrame.setVisible(true);
		}
	}
	
	private void allowPause() {
		synchronized(lock) {
			while(isPaused) {
				try {
					lock.wait();
				} catch(InterruptedException e) {
					// nothing
				}
			}
		}
	}
	
	private String upload() throws UploadException,IOException {
		Uploader uploader = new Uploader();
		uploader.setMaxFileSize(MAX_FILE_SIZE);
		uploader.setName(captchan.tfName.getText());
		uploader.setEmail(captchan.tfEmail.getText());
		uploader.setSubject(captchan.tfSubject.getText());
		uploader.setComment(getComment());
		uploader.setChallenge(reChallenges.removeFirst());
		uploader.setResponse(reResponses.removeFirst());
		uploader.setFile(files[workingOn]);
		uploader.setPostURL(postURL);
		uploader.setTopicURL(topicURL);
		uploader.setThread(captchan.tfThread.getText());
		uploader.setUserAgent(userAgent);
		
		return uploader.upload();
	}
	
	private String getComment() {
		// combine both comment boxes into one string
		String comment = captchan.tfComment.getText();
		if(captchan.btnSend.isToggledOn()) {
			comment += "\r\n" + captchan.taMsg.getText();
			isTextSent = true;
		}
		comment = numberReplace(comment,workingOn);
		return comment;
	}
	
	private void clearCommentArea() {
		if(captchan.btnSend.isToggledOn() && !captchan.btnSend.isChecked() && isTextSent) {
			// if one-time message
			captchan.btnSend.setToggledOn(false);
			captchan.taMsg.setText("");
		}
		isTextSent = false;
	}
	
	private void updateFiles(final int i) {
		// Remove completed files
		boolean isEveryFileUploaded = (i == files.length - 1);
		if(isEveryFileUploaded) {
			files = null;
			captchan.btnSelect.setText("Select files...");
		} else {
			File temp[] = new File[files.length - i];
			System.arraycopy(files,i,temp,0,temp.length);
			files = temp;
			captchan.btnSelect.setText(Integer.toString(files.length) + " Selected...");
		}
	}
	
	private void done() {
		if(isBroken) {
			log("Broken");
		} else {
			captchan.btnSelect.setText("Select Files...");
			files = null;
			log("Done");
		}
		useFastCloseListener();
		isStarted = false;
		captchan.btnAction.setText("Start");
		captchan.setComponentsEnabled(true);
	}
	
	private void resetTitles() {
		captchan.frame.setTitle("Captchan");
		reCaptcha.reFrame.setVisible(false);
		reCaptcha.reFrame.setTitle("Enter Captcha");
	}
	
	private void getResourceThread(String page) {
		// Get the newly created thread's ID
		int left = page.indexOf("<!-- thread:0,no:") + "<!-- thread:0,no:".length();
		boolean isThreadAvailable = (left != -1);
		if(isThreadAvailable) {
			int right = page.indexOf(" ",left);
			captchan.tfThread.setText(page.substring(left,right));
			topicURL += "/res/" + captchan.tfThread.getText();
		} else {
			log("Problem with initial post");
			System.err.println(page);
			isBroken = true;
		}
	}
	
	private String numberReplace(String comment, int i) {
		Matcher match = pattern.matcher(comment);
		StringBuffer sb = new StringBuffer();
		while(match.find()) {
			int offset = 0;
			try {
				offset = Integer.parseInt(match.group(1));
			} catch(NumberFormatException e) {
				// Do nothing
			}
			match.appendReplacement(sb,Integer.toString(1 + i + offset));
		}
		return match.appendTail(sb).toString(); // works even if no replacement is made
	}
	
	private void sleep() {
		long timeToSleep;
		try {
			timeToSleep = Long.parseLong(captchan.tfSeconds.getText());
		} catch(NumberFormatException e) {
			captchan.tfSeconds.setText("90");
			timeToSleep = 90L;
		}
		timeToSleep *= 1000L;
		
		long timeStartedAt = System.currentTimeMillis();
		synchronized(lock) {
			while(!isBroken) {
				try {
					lock.wait(timeToSleep);
					break;
				} catch(InterruptedException e) {
					long timeInterruptedAt = System.currentTimeMillis();
					long timePassed = timeInterruptedAt - timeStartedAt;
					timeToSleep -= timePassed;
				}
			}
		}
	}
	
	private void useConfirmStopListener() {
		captchan.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		captchan.frame.addWindowListener(actConfirmStop);
	}
	
	private void useFastCloseListener() {
		captchan.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		captchan.frame.removeWindowListener(actConfirmStop);
	}
}
