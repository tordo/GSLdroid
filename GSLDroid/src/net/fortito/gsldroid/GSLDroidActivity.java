package net.fortito.gsldroid;

import net.fortito.gsldroid.GOMStreamGrabber.GOMStreamException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

public class GSLDroidActivity extends Activity 
{
	//////////////////////////////////////////////
	//////////// Widgets from layout /////////////
	//////////////////////////////////////////////
	private VideoView m_vv_videoview;
	
	
	//////////////////////////////////////////////
	////////////// Class variables ///////////////
	//////////////////////////////////////////////	
	private GOMStreamGrabber m_grabber;
	
	
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

        m_vv_videoview = (VideoView) findViewById(R.id.main_videoview);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("GSLDROID_USERNAME","");
        String password = prefs.getString("GSLDROID_PASSWORD","");
        
        m_grabber = new GOMStreamGrabber(m_vv_videoview,username, password, GOMStreamGrabber.QUALITY_SQ_TEST);
        
     //   startVideo();
	
    }
    
    /**
     * Called when options menu item is selected
     */
    @Override
    public boolean 
    onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.main_menu_settings:
    		showSettings();
    		break;
    	case R.id.main_menu_play:
    		startVideo();
    		break;
    	default:
    		return false;
    	}
    return true;
    }
    /**
     * Called when options menu is to be created
     */
    @Override
    public boolean 
    onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.main_menu_options, menu);
    	return true;
    }
    
	//////////////////////////////////////////////
	/////////////// Private methods //////////////
	//////////////////////////////////////////////
    
    /**
     * Show settings dialog
     */
    private void 
    showSettings()
    {
    	Intent i = new Intent(this,GSLDroidSettingsActivity.class);
    	startActivity(i);
    }
    
    /**
     * Connect to GOM stream and start viewing
     */
    private void
    startVideo()
    {

    	new Thread(new Runnable() {

			public void run() {
				try {
					m_grabber.startStream();
				} catch (GOMStreamException e) {
					e.printStackTrace();
				}
			}
    	
    	}).start();
    }
}