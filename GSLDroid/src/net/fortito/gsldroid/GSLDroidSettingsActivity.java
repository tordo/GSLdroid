package net.fortito.gsldroid;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class GSLDroidSettingsActivity extends PreferenceActivity 
{
	
	@Override
	public void
	onBuildHeaders(List<Header> headers)
	{
		loadHeadersFromResource(R.xml.preference_headers, headers);
	}
	
	public static class LoginInfoFragment extends PreferenceFragment 
	{
		@Override
		public void
		onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_screen);
		}
		
	}

}
