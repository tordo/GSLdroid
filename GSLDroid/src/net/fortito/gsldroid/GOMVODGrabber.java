package net.fortito.gsldroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import net.fortito.gsldroid.GOMStreamGrabber.GOMStreamException;

public class GOMVODGrabber extends GOMStreamGrabber {

	public GOMVODGrabber(Activity act, String username,
			String password, String quality) {
		super(act, username, password, quality);
	}

	public String getVODUrl(String vod_page) throws GOMStreamException 
	{
		String url = "";
		String vod_page_contents = "";
		String vod_string = "";
		String goxxml = "";
		String key;
		GoxInfo g;
		// Log in
		login();
		// Get VOD page
		try {
			vod_page_contents = getPage(vod_page);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Get VOD string
		vod_string = getVODString(vod_page_contents);
		vod_string += "&strLevel=" + m_quality;
		
		// Get the GOX XML
		try {
			goxxml = getPage("http://www.gomtv.net/gox/ggox.gom?" + vod_string);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		g = parse_gox(goxxml);
		
		key = getKey(g);
		
		
		return g.url +  "&key=" + key;
	}
	
	protected String getVODString(String vodpage) throws GOMStreamException
	{
				Pattern vod_pattern = Pattern.compile("FlashVars=\"(.*)\" quality");
				Matcher m = vod_pattern.matcher(vodpage);
				if(m.find())
				{
					String vodstring = m.group(1);
					return vodstring;
				}
				else
					throw new GOMStreamException(GOMStreamException.ERROR_PARSE_LIVE_PAGE, 
							"Could not find VOD String");
	
	}
	protected GoxInfo parse_gox(String goxxml) throws GOMStreamException
	{
		GoxInfo g = new GoxInfo();
		Pattern gox_pattern = Pattern.compile("<REF href=\"(.*)\" />\\s+<UNO>(\\d*)</UNO>\\s+<NODEID>(\\d*)</NODEID>\\s+<SETID>(\\d*)</SETID>\\s+<NODEIP>(.*)</NODEIP>\\s+<USERIP>(.*)</USERIP>");
		Matcher m = gox_pattern.matcher(goxxml);
		if(m.find()){
			g.url = m.group(1);
			g.uno = m.group(2);
			g.nodeid = m.group(3);
			g.setid = m.group(4);
			g.nodeip = m.group(5);
			g.userip = m.group(6);
			g.url = g.url.replaceAll("&amp;", "&");
			return g;
		}
		else throw new GOMStreamException(GOMStreamException.ERROR_GET_GOX_XML, "Could not parse VOD GOXXML");
		
		
	}
	
	private String getKey(GoxInfo g) throws GOMStreamException
	{
		byte[] buffer = new byte[256];
		String key;
		String[] key_split;
		String login_string = "Login,0," + g.uno + "," + g.nodeid + "," + g.userip+ "\n";
		OutputStream sockout;
		InputStream sockin;
		try {
			Socket s = new Socket(g.nodeip, 63800);
			sockout = s.getOutputStream();
			sockin = s.getInputStream();
			sockout.write(login_string.getBytes());
			sockin.read(buffer);
			s.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		key = new String(buffer);
		key_split = key.split(",");
		if(key_split.length < 5) throw new GOMStreamException(GOMStreamException.ERROR_PARSE_LIVE_PAGE, "Could not get key");
		return key_split[5].trim();
		
		
	}
	
	public void startVod(String url) throws GOMStreamException
	{
		String vodurl = "";
	
		vodurl = getVODUrl(url);
		
		playVod(vodurl);
	}
	private void
	playVod(String streamURL)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(streamURL),"application/x-mpegURL" );
		m_activity.startActivity(i);
	}
	
	public ArrayList<VodInfo> parseVodListPage(String page)
	{
		ArrayList<VodInfo> vodlist = new ArrayList<VodInfo>();
		VodInfo v;
		Pattern p = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
		Matcher m = p.matcher(page);
		while(m.find()){
			v = VodInfo.fromTableHtml(m.group(1));
			if(v != null)
				vodlist.add(v);
		}
		return vodlist;
	}
	public ArrayList<VodInfo> getVodList(int page) throws GOMStreamException
	{
		String url = "http://www.gomtv.net/videos/index.gom?order=1&limit=10&subtype=-1&page=" + Integer.toString(page);
		String pageContents = "";
		try {
			pageContents = getPage(url);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parseVodListPage(pageContents);
		
	}
	public int getNumberOfSets(String vodurl) throws GOMStreamException {
		String pageContents = "";
		try {
			pageContents = getPage(vodurl);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Pattern p = Pattern.compile("<a href=\"#\" onclick=\"moveSet\\('.*?'\\); return false;\" title=\"(\\d*)\">", Pattern.DOTALL);
		Matcher m = p.matcher(pageContents);
		int ret = 0;
		while(m.find()) ret++;
		
		return ret;
		
		
		
	}
	
	
	private class GoxInfo {
		public String url;
		public String uno;
		public String nodeid;
		public String setid;
		public String nodeip;
		public String userip;
		public GoxInfo()
		{
		}
	}



	
}
