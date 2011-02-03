import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

class ClientHttpRequest {
	
	private final URLConnection conn;
	private final String referer;
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private String userAgent;
	
	private void write(String s) throws IOException {
		baos.write(s.getBytes());
	}
	
	private void newline() throws IOException {
		baos.write("\r\n".getBytes());
	}
	
	private void writeln(String s) throws IOException {
		write(s);
		newline();
	}
	
	private String randomString() {
		Random random = new Random();
		return Long.toString(random.nextLong(),36);
	}
	
	private final String boundary = "---------------------------" + randomString()
	    + randomString() + randomString();
	
	private void boundary() throws IOException {
		write("--" + boundary);
	}
	
	public ClientHttpRequest(String urlString, String referer) throws IOException {
		this.conn = new URL(urlString).openConnection();
		this.referer = referer;
	}
	
	private void writeName(String name) throws IOException {
		newline();
		write("Content-Disposition: form-data; name=\"" + name + "\"");
	}
	
	public void setParameter(String name, String value) throws IOException {
		boundary();
		writeName(name);
		newline();
		newline();
		writeln(value);
	}
	
	private void pipe(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[500000];
		int nread;
		synchronized(in) {
			while((nread = in.read(buf,0,buf.length)) >= 0) {
				out.write(buf,0,nread);
			}
		}
		out.flush();
		in.close();
	}
	
	private void setParameter(String paramName, String filename, InputStream is)
	    throws IOException {
		boundary();
		writeName(paramName);
		writeln("; filename=\"" + filename + "\"");
		String type = URLConnection.guessContentTypeFromName(filename);
		if(type == null) {
			type = "application/octet-stream";
		}
		writeln("Content-Type: " + type);
		newline();
		pipe(is,baos);
		is.close();
		newline();
	}
	
	public void setParameter(String paramName, File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		setParameter(paramName,file.getPath(),fis);
		fis.close();
	}
	
	public String post() throws IOException {
		boundary();
		writeln("--");
		
		conn.setDoOutput(true);
		conn.setRequestProperty("Host","sys.4chan.org");
		
		conn.setRequestProperty("User-Agent",userAgent);
		conn.setRequestProperty("Accept",
		    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language","en-us,en;q=0.5");
		conn.setRequestProperty("Accept-Encoding","gzip,deflate");
		conn.setRequestProperty("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		conn.setRequestProperty("Keep-Alive","115");
		conn.setRequestProperty("Connection","keep-alive");
		conn.setRequestProperty("Referer",referer);
		conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
		conn.setRequestProperty("Content-Length",Integer.toString(baos.size()));
		OutputStream os = conn.getOutputStream();
		os.write(baos.toByteArray());
		os.close();
		
		String encoding = conn.getHeaderField("Content-Encoding");
		return convertStreamToString(conn.getInputStream(),encoding);
	}
	
	private String convertStreamToString(InputStream is, String encoding)
	    throws IOException {
		if(is == null)
			return null;
		
		if(encoding.equals("gzip")) {
			is = new GZIPInputStream(is);
		} else if(encoding.equals("deflate")) {
			is = new InflaterInputStream(is,new Inflater(true));
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null)
			sb.append(line).append("\n");
		
		is.close();
		return sb.toString();
	}
	
	public static String get(URL url) {
		InputStream is = null;
		try {
			URLConnection conn = url.openConnection();
			is = conn.getInputStream();
		} catch(IOException e) {
			System.err.println("URL is malformed and can't be loaded");
		}
		return new Scanner(is).useDelimiter("\\Z").next();
	}
	
	void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}
