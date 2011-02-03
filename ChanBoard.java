import java.net.MalformedURLException;
import java.net.URL;

class ChanBoard {
	
	static String getMaxFileSize(String topicURL) {
		String result = null;
		try {
			String page = ClientHttpRequest.get(new URL(topicURL));
			int left = page.indexOf("MAX_FILE_SIZE\" value=\"")
			    + "MAX_FILE_SIZE\" value=\"".length();
			int right = page.indexOf("\"",left);
			result = page.substring(left,right);
		} catch(MalformedURLException e) {
			System.err.println("Thread URL is malformed and can't be loaded");
		}
		return result;
	}
}
