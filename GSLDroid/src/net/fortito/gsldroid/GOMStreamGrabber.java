package net.fortito.gsldroid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import android.net.http.AndroidHttpClient;
import android.util.Log;
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
	public static final String QUALITY_SQ= "SQ";
	/** Stream Quality: HQ */
	public static final String QUALITY_HQ = "HQ";
	
	/** VideoView to show the video in */
	private VideoView m_videoview;
	/** Http client to do our requests */
	private AndroidHttpClient m_httpclient;
	/** HttpContext to keep our cookies */
	private BasicHttpContext m_httpcontext;
	/** Cookie store, member variable to ease debugging */
	private BasicCookieStore m_cookies;
	
	/** Tag for logging */
	private static final String TAG = "GOMStreamGrabber";
	
	/** GOMTV Login page */
	private static final String GOM_LOGIN_PAGE = "https://ssl.gomtv.net/userinfo/loginProcess.gom";
	
	/** Constructor
	 * 
	 * @param videoview VideoView to display stream in
	 * @param username GOMTV username
	 * @param password GOMTV password
	 */
	public GOMStreamGrabber(VideoView videoview, String username, String password, String quality)
	{
		m_videoview = videoview;
		m_username = username;
		m_password = password;
		m_quality = quality;
		m_httpclient = AndroidHttpClient.newInstance(null);
		m_httpcontext = new BasicHttpContext();
		m_cookies = new BasicCookieStore();
		m_httpcontext.setAttribute(ClientContext.COOKIE_STORE, m_cookies);
	}
	
	/**
	 * Start the stream. This does:
	 * - Login
	 * - Gets live page
	 * - Parses live page to get stream URL
	 * - Fires up a MediaPlayer to play the stream
	 * @throws GOMStreamException
	 */
	public void 
	startStream() throws GOMStreamException
	{
		login();
		getLivePage();
		parseLivePage();			
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
	
	private String
	getLivePage() throws GOMStreamException
	{
		
		
		return "";
	}
	
	private void 
	parseLivePage() throws GOMStreamException
	{
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
