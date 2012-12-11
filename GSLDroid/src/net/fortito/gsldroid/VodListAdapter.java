package net.fortito.gsldroid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.fortito.gsldroid.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;


public class VodListAdapter extends ArrayAdapter<VodInfo>
{

	
	public VodListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	public View getView (int position, View convertView, ViewGroup parent)
	{
		final Handler h = new Handler();
		View v;
		if(convertView != null) v = convertView;
		else {
			LayoutInflater i = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = i.inflate(R.layout.vod_item, null);
		}
		final VodInfo info = getItem(position);
		TextView tournament, name, players, date, rating; 
		final ImageView img;
		tournament = (TextView)v.findViewById(R.id.tv_tournament);
		name = (TextView)v.findViewById(R.id.tv_name);
		players = (TextView)v.findViewById(R.id.tv_players);
		date = (TextView)v.findViewById(R.id.tv_date);
		img = (ImageView)v.findViewById(R.id.imageView1);
		
		tournament.setText(info.tournament);
		name.setText(info.name);
		players.setText(info.players);
		date.setText(info.date);
		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				URL newurl = null;
				try {
					newurl = new URL(info.imgUrl);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					final Bitmap b = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
					h.post(new Runnable() {
						public void run() {
							img.setImageBitmap(b);
						}
					});


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			} }).start();
		
		
		
		return v;
		
		
	}
	
}
