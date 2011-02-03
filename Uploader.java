import java.io.File;
import java.io.IOException;

class Uploader {
	private File file;
	private String MAX_FILE_SIZE;
	private String thread;
	private String postURL;
	private String topicURL;
	private String comment;
	private String name;
	private String email;
	private String subject;
	private String challenge;
	private String response;
	private String userAgent;
	
	public Uploader() {
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setMaxFileSize(String maxFileSize) {
		MAX_FILE_SIZE = maxFileSize;
	}
	
	public void setThread(String thread) {
		this.thread = thread;
	}
	
	public void setPostURL(String postURL) {
		this.postURL = postURL;
	}
	
	public void setTopicURL(String topicURL) {
		this.topicURL = topicURL;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String upload() throws UploadException,IOException {
		
		if(isFileTooLarge())
			throw new FileTooLargeException();
		
		ClientHttpRequest chr = new ClientHttpRequest(postURL,topicURL);
		
		if(!thread.equals("")) {
			chr.setParameter("resto",thread);
		}
		chr.setParameter("MAX_FILE_SIZE",MAX_FILE_SIZE);
		chr.setParameter("name",name);
		chr.setParameter("email",email);
		chr.setParameter("sub",subject);
		chr.setParameter("com",comment);
		chr.setParameter("recaptcha_challenge_field",challenge);
		chr.setParameter("recaptcha_response_field",response);
		chr.setParameter("upfile",file);
		chr.setParameter("pwd",genPass());
		chr.setParameter("mode","regist");
		chr.setUserAgent(userAgent);
		String result = chr.post();
		
		handleExceptions(result);
		
		return result;
	}
	
	private boolean isFileTooLarge() {
		long maxSize;
		try {
			maxSize = Long.parseLong(MAX_FILE_SIZE);
		} catch(NumberFormatException e) {
			return false;
		}
		return(file.length() > maxSize);
	}
	
	private void handleExceptions(String result) throws UploadException {
		System.out.println(result);
		if(result.length() < 500)
			return;
		
		if(result.indexOf("You seem to have mistyped the verification.") != -1) {
			throw new BadCaptchaException();
		} else if(result.indexOf("Flood detected,") != -1) {
			throw new FloodException();
		} else if(result.indexOf("Max limit of ") != -1) {
			throw new MaxLimitException();
		} else if(result.indexOf("Thread specified does not exist") != -1) {
			throw new Error404Exception();
		} else if(result.indexOf("Detected possible malicious code in image file") != -1) {
			throw new MaliciousFileException();
		} else if(result.indexOf("Upload failed") != -1) {
			throw new UploadFailException();
		}
		throw new UploadException();
	}
	
	private String genPass() {
		// avoid abuse by generating new passwords every time
		String result = "";
		for(int i = 0;i < 8;i++)
			result += (char)(((int)Math.random() * 26) + 97);
		return result;
	}
}
