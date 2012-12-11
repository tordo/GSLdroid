package net.fortito.gsldroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VodInfo {
	public String url;
	public String name;
	public String tournament;
	public String players;
	public String date;
	public String imgUrl;
	public String rating;
	
	public VodInfo() {
		 
	}
	public static VodInfo fromTableHtml(String table)
	{
		VodInfo ret = new VodInfo();
		
		Pattern p = Pattern.compile(".*class=\"vod_link\" title=\"(.*?)\" href=\"(.*?)\".*src=\"(.*?)\".*(\\[.*\\]).*<span class=\"m_name\">(.*?)</span>.*Date : <strong>(.*?)</strong>", Pattern.DOTALL);
		Matcher m = p.matcher(table);
		if(m.find())
		{
			ret.name = m.group(1).trim();
			ret.url = "http://www.gomtv.net" + m.group(2).trim() + "/";
			ret.imgUrl = m.group(3).trim();
			ret.tournament = m.group(4).trim();
			ret.players = m.group(5).trim();
			ret.date = m.group(6).trim();
			ret.rating = "";
			return ret;
		}
		return null;
		
	}
}
