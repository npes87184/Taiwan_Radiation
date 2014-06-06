package com.example.final_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	boolean first = true;
	private ListView listView;
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView)findViewById(R.id.listView1);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//download
				//InputStream source = getResources().getAssets().open("gammamonitor.csv");
				try {
					ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo info = CM.getActiveNetworkInfo();
					if((info != null) && info.isConnected()) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
						adapter.add("地點  監測值(微西弗/時)");
						String line;
						while((line = reader.readLine())!=null) {
							String [] data = line.split(",");
							if(first) {
								first = false;
								continue;
							}
							adapter.add(data[0]+ "  " + data[2]);
						}
					}
					else {
						adapter.add("沒有網路，請開啟網路後重新啟動應用。");
					}
				} catch (IOException e) {
				    e.printStackTrace();
				} catch (URISyntaxException e) {
					
				}

				
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listView.setAdapter(adapter);
				}
				});
		   }
		}).start();
		
	}

	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.aec.gov.tw/open/gammamonitor.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
	
