package net.fortito.gsldroid;
import java.util.ArrayList;

import net.fortito.gsldroid.GOMStreamGrabber.GOMStreamException;
import net.fortito.gsldroid.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;


public class PickVodActivity extends Activity {
	ListView m_listview;
	VodListAdapter m_vodlistadapter;
	GOMVODGrabber m_vodgrabber;
	Handler m_handler = new Handler();
	Button m_but_next;
	Button m_but_prev;
	int m_page = 1;
    public void 
    onCreate(Bundle savedInstanceState) 
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("GSLDROID_USERNAME","");
        String password = prefs.getString("GSLDROID_PASSWORD","");
        String quality = prefs.getString("GSLDROID_QUALITY", GOMStreamGrabber.QUALITY_SQ);

    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_list);  
        m_listview = (ListView)findViewById(R.id.lv_vodlist);
        m_vodlistadapter = new VodListAdapter(this, 0);
        m_listview.setAdapter(m_vodlistadapter);
        m_listview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				VodInfo v = m_vodlistadapter.getItem(arg2);
				Intent i = new Intent(PickVodActivity.this, PlayVodActivity.class);
				i.putExtra("VODURL", v.url);
				i.putExtra("VODPLAYERS", v.players);
				i.putExtra("VODTOURNAMENT", v.tournament);
				i.putExtra("VODNAME", v.name);
				startActivity(i);
			}
		});
        m_vodgrabber = new GOMVODGrabber(this,username, password, quality);
        m_but_next = (Button)findViewById(R.id.bt_next);
        m_but_prev = (Button)findViewById(R.id.bt_prev);
        
        m_but_next.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				m_page++;
				fetchVods();
				
			}
		});

        m_but_prev.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				m_page--;
				
				fetchVods();
				
			}
		});
        fetchVods();
    }

	private void fetchVods() {
		if(m_page == 1) m_but_prev.setVisibility(View.INVISIBLE);
		else m_but_prev.setVisibility(View.VISIBLE);
		// Show spinner
		final ProgressDialog d = new ProgressDialog(this);
		d.setMessage("Fetching VOD list");
		final int p = m_page;
		d.setIndeterminate(true);
		d.show();
		new Thread(new Runnable(){

			public void run() {
				try {
					final ArrayList<VodInfo> list = m_vodgrabber.getVodList(p);
					m_handler.post(new Runnable() {

						public void run() {
							m_vodlistadapter.clear();
							m_vodlistadapter.addAll(list);							
						} });
					
					
				} catch (GOMStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				d.dismiss();
			}}).start();
	}
}
