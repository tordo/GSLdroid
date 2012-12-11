package net.fortito.gsldroid;

import net.fortito.gsldroid.GOMStreamGrabber.GOMStreamException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class PlayVodActivity extends Activity {
	TextView m_tv_tournament;
	TextView m_tv_name;
	TextView m_tv_players;
	Spinner m_spin_quality;
	Spinner m_spin_set;
	GOMVODGrabber m_vodgrabber;
	String m_vodurl;
	Handler m_handler = new Handler();
	Button m_but_go;
	
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		Intent i = getIntent();
		setContentView(R.layout.play_vod_layout);
		final ProgressDialog d = new ProgressDialog(this);
		d.setMessage("Loading...");
		d.setIndeterminate(true);
		d.show();
		m_tv_tournament = (TextView)findViewById(R.id.tv_matchtournament);
		m_tv_name = (TextView)findViewById(R.id.tv_matchname);
		m_tv_players = (TextView)findViewById(R.id.tv_matchplayers);
		m_spin_quality = (Spinner)findViewById(R.id.spin_matchquality);
		m_spin_set = (Spinner)findViewById(R.id.spin_matchset);
		m_but_go = (Button)findViewById(R.id.but_matchgo);
		m_tv_tournament.setText(i.getStringExtra("VODTOURNAMENT"));
		m_tv_name.setText(i.getStringExtra("VODNAME"));
		m_tv_players.setText(i.getStringExtra("VODPLAYERS"));
		
		m_vodurl = i.getStringExtra("VODURL");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    String username = prefs.getString("GSLDROID_USERNAME","");
	    String password = prefs.getString("GSLDROID_PASSWORD","");
	    String quality = prefs.getString("GSLDROID_QUALITY", GOMStreamGrabber.QUALITY_SQ);
	    m_vodgrabber = new GOMVODGrabber(this, username, password, quality);
		final ArrayAdapter<String> qualities = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
	    qualities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    qualities.add(GOMStreamGrabber.QUALITY_SQ_TEST);
	    qualities.add(GOMStreamGrabber.QUALITY_SQ);
	    qualities.add(GOMStreamGrabber.QUALITY_HQ);
	    m_spin_quality.setAdapter(qualities);
	    m_spin_quality.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				m_vodgrabber.setQuality(qualities.getItem(arg2));

				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}	});

	    for(int j = 0; j < qualities.getCount(); j++)
	    {
	     	if(quality.equals(qualities.getItem(j)))
	       	{
	       		m_spin_quality.setSelection(j);
	       	}
	       		
	    }
	    new Thread(new Runnable() {

			public void run() {
				 	int n_sets = 0;
				    try {
						n_sets = m_vodgrabber.getNumberOfSets(m_vodurl);
					} catch (GOMStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    final int fn_sets = n_sets;
				   
				    
				    m_handler.post(new Runnable() 
				    	{

							public void run() {
								  ArrayAdapter<String> sets = new ArrayAdapter<String>(PlayVodActivity.this, android.R.layout.simple_spinner_item);
								    for(int j = 0; j < fn_sets; j++)
								    {
								    	sets.add(Integer.toString(j+1));
								    }
							    m_spin_set.setAdapter(sets);
							    
							    d.dismiss();
								
							}
				    	
				    	});
				    }
		
	    	 }).start();

	
	    m_but_go.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				final ProgressDialog d = new ProgressDialog(PlayVodActivity.this);
				d.setIndeterminate(true);
				d.setMessage("Starting VOD...");
				d.show();
				new Thread(new Runnable() {

					public void run() {
						String url = m_vodurl + "?set=" + m_spin_set.getSelectedItem();
						try {
							m_vodgrabber.startVod(url);
						} catch (final GOMStreamException e) {
							// TODO Auto-generated catch block
							d.dismiss();
							m_handler.post(new Runnable() {
								public void run()
								{

									AlertDialog.Builder adb = new AlertDialog.Builder(PlayVodActivity.this); 
									
									adb.setMessage(e.getMessage());
									adb.show();
								}
							});
						}
						d.dismiss();
						
					}
					
				}).start();
				
			}
		});
	}
	

}
