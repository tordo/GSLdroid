package net.fortito.gsldroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.VideoView;

/**
 * This class takes care of talking to the gomtv webpage, get the stream link, and get the stream up and running in the webview.
 * Pretty much everything in here is ripped right off GOMstreamer by sjp,
 * https://github.com/sjp/GOMstreamer/
 * Big thank you to him.
 * @author tord
 *
 */
public class GOMStreamGrabber {

	/** GOMTV Username */
	private String m_username;
	/** GOMTV Password */
	private String m_password;
	/** Video quality */
	private String m_quality;
	
	/** Stream Quality: SQ TEST */
	public static final String QUALITY_SQ_TEST = "SQTest";
	/** Stream Quality: SQ */
	public static final String QUALITY_SQ = "SQ";
	/** Stream Quality: HQ */
	public static final String QUALITY_HQ = "HQ";
	

	/** Http client to do our requests */
	private AndroidHttpClient m_httpclient;
	/** HttpContext to keep our cookies */
	private BasicHttpContext m_httpcontext;
	/** Cookie store, member variable to ease debugging */
	private BasicCookieStore m_cookies;
	
	/** Context */
	private GSLDroidActivity m_activity;
	
	/** Tag for logging */
	private static final String TAG = "GOMStreamGrabber";
	
	/** GOMTV Login page */
	private static final String GOM_LOGIN_PAGE = "https://ssl.gomtv.net/userinfo/loginProcess.gom";
	private static final String GOM_HOME_PAGE = "http://www.gomtv.net/";
	/** Constructor
	 * 
	 * @param videoview VideoView to display stream in
	 * @param username GOMTV username
	 * @param password GOMTV password
	 */
	public GOMStreamGrabber(GSLDroidActivity act, String username, String password, String quality)
	{
		m_activity = act;
		m_username = username;
		m_password = password;
		m_quality = quality;
		m_httpclient = AndroidHttpClient.newInstance(null);
		m_httpcontext = new BasicHttpContext();
		m_httpclient.getParams().setBooleanParameter("http.protocol.handle-redirects", true);
		m_cookies = new BasicCookieStore();

		m_httpcontext.setAttribute(ClientContext.COOKIE_STORE, m_cookies);
	}
	
	/**
	 * Start the stream. This does:
	 * - Login
	 * - Gets live page
	 * - Parses live page to get stream URL
	 * - Fires up a MediaPlayer to play the stream
	 * Uses the 
	 * @throws GOMStreamException
	 */
	public void 
	startStream() throws GOMStreamException
	{
		m_activity.showUserMsg("Logging in");
		login();
		
		String livepage = "";
		m_activity.showUserMsg("Getting live page");
		try {
			livepage = getPage(getLivePageURL(null));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String goxxml = getGOXXML(livepage);
		String url = "";
		m_activity.showUserMsg("Getting GOX XML");
		try {
			url = getStreamURL(getPage(goxxml));
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw new GOMStreamException(GOMStreamException.ERROR_GET_GOX_XML, "Could not get GOX XML");
		} catch (IOException e) {
			e.printStackTrace();
			throw new GOMStreamException(GOMStreamException.ERROR_GET_GOX_XML, "Could not get GOX XML");
		}
		m_activity.showUserMsg("Starting stream");
		playStream(url);
		m_activity.showUserMsg("");
	}
	
	/**
	 * Login to GOMtv.net
	 * This function logs in to GOMtv.net, and the login cookies are set in m_httpcontext/m_cookies as a result.
	 * @throws GOMStreamException
	 */
	private void 
	login() throws GOMStreamException
	{
		// Build HTTP Request
		HttpResponse resp = null;
		HttpPost req = new HttpPost(GOM_LOGIN_PAGE);
		// Referer needs to be set
		req.addHeader(new BasicHeader("Referer", "http://www.gomtv.net/"));
		// Build POST parameters
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("cmd","login"));
		params.add(new BasicNameValuePair("rememberme", "1"));
		params.add(new BasicNameValuePair("mb_username", m_username));
		params.add(new BasicNameValuePair("mb_password", m_password));
		try {
			req.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new GOMStreamException(GOMStreamException.ERROR_LOGIN, "Couldn't build POST parameter list, malformed username/password?");
		}
		// Execute request
		try {
			resp = m_httpclient.execute(req, m_httpcontext);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GOMStreamException(GOMStreamException.ERROR_LOGIN,"Login error");
		}
		// If we don't get a response, fail
		if(resp == null)
			throw new GOMStreamException(GOMStreamException.ERROR_LOGIN, "Response was null");

		// If we didn't get any cookies, fail
		if(m_cookies.getCookies().size() == 0)
			throw new GOMStreamException(GOMStreamException.ERROR_LOGIN, "Did not get cookies, wrong username/password?");
		
		Log.d(TAG, "Logged in!");
	}
	
	/**
	 * Get the contents of a page, using the local httpcontext
	 * @param URL The url of the page to get
	 * @return The contents of the page
	 */
	private String
	getPage(String URL) throws IOException,IllegalStateException
	{
		HttpGet req = new HttpGet(URL);
		HttpResponse response = m_httpclient.execute(req,m_httpcontext);
	
		InputStream is = response.getEntity().getContent();
	
		byte buf[] = new byte[1024];
		String ret = "";
	
		while(is.read(buf) != -1)
		{
				ret += new String(buf);
		}
		return ret;
		
	}
	
	
	/** Get Live page URL
	 * Get the URL of the Live page
	 */
	private String getLivePageURL(String frontpage)
	{
		// FIXME: Make me more general
		return "http://www.gomtv.net/main/goLive.gom";	
	}

	/**
	 * Get the GOXXML URL from the Live page. Grabs the GOXXML according to the selected quality
	 * @param livepage The contents of the live page
	 * @return the GOX XML URL
	 */
	private String 
	getGOXXML(String livepage) throws GOMStreamException
	{
		// FIXME: Support for multiple streams
		Pattern gox_pattern = Pattern.compile("[^/]+var.+(http://www.gomtv.net/gox[^;]+;)");
		Matcher m = gox_pattern.matcher(livepage);
		if(m.find())
		{
			String partialUrl = m.group(1);
			partialUrl = partialUrl.replaceFirst("\" \\+ playType \\+ \"",m_quality);
			partialUrl = partialUrl.replaceFirst("\"\\+ tmpThis.title \\+\"&\";","GSL");
			partialUrl = partialUrl.replaceAll("&amp;","&");
			return partialUrl;
			
		}
		else
			throw new GOMStreamException(GOMStreamException.ERROR_PARSE_LIVE_PAGE, 
					"Could not find GOX XML - this usually means GSL is not live at the moment");
	}
	/**
	 * Get the stream URL from the gox xml
	 * @param goxxml the contents of the GOX XML
	 * @return the URL of the stream
	 */ 
	private String
	getStreamURL(String goxxml)
	{
		Pattern stream_pattern = Pattern.compile("<REF href=\"([^\"]*)\"\\s*/>");
		Matcher m = stream_pattern.matcher(goxxml);
		String gomcmd = "";
		if(m.find()){
			gomcmd = m.group(1);
		}
		gomcmd = Uri.decode(gomcmd);
		if(gomcmd.startsWith("gomp2p://")) 
		{
			gomcmd = gomcmd.replaceFirst("^.*LiveAddr=", "");
		}
		gomcmd = gomcmd.replaceAll("&amp;", "&");
		gomcmd = gomcmd.replace("&quot;","");
		return gomcmd;
		
	}
	
	private void
	playStream(String streamURL)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(streamURL),"application/x-mpegURL" );
		m_activity.startActivity(i);
	}
	
		
	/** Exception class for GOMStreamGrabber */
	public static class GOMStreamException extends Exception {
		private static final long serialVersionUID = -2312236397084877069L;
		/** Error in login procedure */
		public static final int ERROR_LOGIN = 1;
		/** Error getting live page */
		public static final int ERROR_GET_LIVE_PAGE = 2;
		/** Error parsing live page */
		public static final int ERROR_PARSE_LIVE_PAGE = 3;
		/** Error getting live page URL */
		public static final int ERROR_GET_LIVE_PAGE_URL = 4;
		/** Error getting the GOX XML */
		public static final int ERROR_GET_GOX_XML = 5;
		/** Exception ID */
		
		int id;
		/**
		 * Constructor
		 * @param id (see class constants)
		 * @param msg (see Exception)
		 */
		public GOMStreamException(int id, String msg){
			super(msg);
			this.id = id;
		}
	}
}
