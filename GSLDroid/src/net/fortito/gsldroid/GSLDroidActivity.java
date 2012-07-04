package net.fortito.gsldroid;

import net.fortito.gsldroid.GOMStreamGrabber.GOMStreamException;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class GSLDroidActivity extends Activity 
{
	//////////////////////////////////////////////
	//////////// Widgets from layout /////////////
	//////////////////////////////////////////////
	EditText m_et_username;
	EditText m_et_password;
	TextView m_tw_status;
	Spinner m_spin_quality;
	Button m_bt_go;
	
	//////////////////////////////////////////////
	//////////// Activity callbacks //////////////
	//////////////////////////////////////////////
	
    /** Called when the activity is first created. */
    @Override
    public void 
    onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("GSLDROID_USERNAME","");
        String password = prefs.getString("GSLDROID_PASSWORD","");
        
        m_et_username = (EditText)findViewById(R.id.et_username);       
        m_et_password = (EditText)findViewById(R.id.et_password);
        m_spin_quality = (Spinner)findViewById(R.id.spin_quality);
        m_tw_status = (TextView)findViewById(R.id.tw_status);
        ArrayAdapter<String> qualities = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        qualities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //qualities.add(GOMStreamGrabber.QUALITY_SQ_TEST);
        qualities.add(GOMStreamGrabber.QUALITY_SQ);
        qualities.add(GOMStreamGrabber.QUALITY_HQ);
        m_spin_quality.setAdapter(qualities);

        m_bt_go = (Button)findViewById(R.id.button1);
        
        m_bt_go.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveAuthData();
				startVideo();
				
			}
		});
        
        
        m_et_username.setText(username);
        m_et_password.setText(password);
     }
    

	//////////////////////////////////////////////
	/////////////// Public methods ///////////////
	//////////////////////////////////////////////
    
    public void
    showUserMsg(final String what)
    {
    	runOnUiThread(new Runnable(){

			public void run() {
				m_tw_status.setText(what);
			}		
    	}
    	);
    }
    
	//////////////////////////////////////////////
	/////////////// Private methods //////////////
	//////////////////////////////////////////////
    /**
     * Save authentication data from text boxes
     */
    private void saveAuthData()
    {
    	String password = m_et_password.getText().toString().trim();
    	String username = m_et_username.getText().toString().trim();
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putString("GSLDROID_USERNAME", username);
    	editor.putString("GSLDROID_PASSWORD",password);
    	editor.commit();
    }
    
    /**
     * Connect to GOM stream and start viewing
     */
    private void
    startVideo()
    {
    	
    	final GOMStreamGrabber grabber = new GOMStreamGrabber(this, m_et_username.getText().toString().trim(), m_et_password.getText().toString().trim(), (String)m_spin_quality.getAdapter().getItem(m_spin_quality.getSelectedItemPosition()));
    	
    	new Thread(new Runnable() {

			public void run() {
				try {
					grabber.startStream();
				} catch (GOMStreamException e) {
					showUserMsg("ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
    	
    	}).start();
    }
    
}