package com.example.final_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.refreshnow.widget.RefreshNowListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnRefreshListener {

	boolean first = true;
	private RefreshNowListView listView;
	ArrayAdapter<String> adapter;
	ArrayList<String> list = new ArrayList<String>();
	Time t = new Time();
	private boolean refreshing = false;
	private static final double version = 1.22;
	private String versionString = " ";
	private static int i;
	private static final String APP_NAME = "Taiwan_Radiation";
	private static final String APP_ENTER_NUMBER = "enter_number";
	
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SysApplication.getInstance().addActivity(this);
		
		prefs = getPreferences(MODE_PRIVATE);

		
		i = prefs.getInt(APP_ENTER_NUMBER, 0);
		i++;
		
		System.out.println(i);
		
		if(i > 2) {
			i = 0;
			new Thread(new Runnable() {  //auto check version
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo info = CM.getActiveNetworkInfo();
						if((info != null) && info.isConnected()) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlDataOta(),"BIG5"));
							String line;
							double temp_version = -1;
							while((line = reader.readLine())!=null) {
								String [] data = line.split(",");
								if(data[0].equals(APP_NAME)) {
									temp_version = Double.parseDouble(data[1]);
									System.out.println(temp_version);
									
									boolean first = true;
									boolean second = true;
									int i = 1;
									for(String aString : data) {
										if(first) {
											first = false;
											continue;
										}
										if(second) {
											versionString = versionString + "新版本號：" + aString;
											versionString = versionString + "\n\n ";
											second = false;
											continue;
										}
										versionString = versionString + "      " + i + "." +aString;
										versionString = versionString + "\n ";
										i++;
									}
									break;
								}
							}
							if(temp_version > version) {
								Message msg = new Message();
								msg.what = 1;
								mHandler.sendMessage(msg);
							}
							else {
								//do nothing
							}
						}
						else {
							//do nothing
						}
					} catch (IOException e) {
						System.out.println("IO");
					} catch (URISyntaxException e) {
						System.out.println("URL");
					}
				}
			}).start();
		}
		
		prefs.edit().putInt(APP_ENTER_NUMBER, i).commit();
		
		listView = (RefreshNowListView)findViewById(R.id.listview1);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setOnRefreshListener(this);
	//	listView.setRefreshIndicatorView(findViewById(android.R.id.progress));
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//download thread
				//InputStream source = getResources().getAssets().open("gammamonitor.csv");
				refresh();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listView.setAdapter(adapter);
					}
				});
			}
		}).start();
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				View view = View.inflate(MainActivity.this, R.layout.ota, null);
				TextView versionChange = (TextView) view.findViewById(R.id.textView1);
				versionChange.setText(versionString);
				versionString = " ";
				TextView textView = (TextView) view.findViewById(R.id.textView3);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle("新版本").setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
            }
          super.handleMessage(msg);
		}
	};
	
	
	//get latest version code
	public InputStream getUrlDataOta() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.cmlab.csie.ntu.edu.tw/~npes87184/version.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
	
	public void refresh() {
		try {
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = CM.getActiveNetworkInfo();
			if((info != null) && info.isConnected()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
				t.setToNow();
				list.clear();
				list.add("更新時間：" + t.hour+ ":" + t.minute + ":" + t.second);
				list.add("地點  監測值(微西弗/時)");
				String line;
				while((line = reader.readLine())!=null) {
					String [] data = line.split(",");
					if(first) {
						first = false;
						continue;
					}
					list.add(data[0]+ "  " + data[2]);
				}
			}
			else {
				list.clear();
				list.add("沒有網路，請開啟網路後重新整理。");
			}
			first = true;
		} catch (IOException e) {
			refresh();
		} catch (URISyntaxException e) {
			refresh();
		}
	}
	
	
	//google style refresh
	@Override
	public void onRefreshComplete() {
		Toast.makeText(this, "重新整理完成", Toast.LENGTH_SHORT).show();
	//	setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onRefreshStart(final RefreshMode mode) {
	//	Toast.makeText(this, "重新整理中", Toast.LENGTH_SHORT).show();
	//	setProgressBarIndeterminateVisibility(true);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!refreshing) {
					refreshing = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							refresh();
						}
					}).start();
					adapter.notifyDataSetChanged();
					listView.setRefreshComplete();
					refreshing = false;
				}
			}
		}, 100);  // 0.1 second
		
	}

	//Download main function.
	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.aec.gov.tw/open/gammamonitor.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"重新整理");
		menu.add(0,1,0,"地圖顯示");
		menu.add(0,2,0,"設定");
		menu.add(0,3,0,"關於");
		menu.add(0,4,0,"離開");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
			case 0:
				if(!refreshing) {
					refreshing = true;
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							refresh();
						}
					}).start();
					adapter.notifyDataSetChanged();
					Toast.makeText(this, "重新整理完成", Toast.LENGTH_SHORT).show();
					refreshing = false;
				}
				break;
			case 1:
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MapView.class);
				startActivity(intent);
				finish();
				break;
			case 2:
				intent = new Intent();
				intent.setClass(MainActivity.this, SettingActivity.class);
				startActivity(intent);
				break;
			case 3:
				View view = View.inflate(MainActivity.this, R.layout.about, null); 
				TextView textView = (TextView) view.findViewById(R.id.textView3);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("關於").setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
			case 4:
				SysApplication.getInstance().exit(); 
				break;
		}
		return true;
	}
}
	
