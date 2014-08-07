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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	String auto_update_key;
	String location_key;
	String update_frequency_key;
	CheckBoxPreference checkBoxPreference;
	ListPreference listPreference1;
	ListPreference listPreference2;
	Item item = new Item();
	Preference otaPreference;
	private ProgressDialog progressDialog;
	private String versionString = " ";
	private static final double version = 1.11;
	private static final String APP_NAME = "Taiwan_Radiation";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		SysApplication.getInstance().addActivity(this);
		auto_update_key = getResources().getString(R.string.alert_key);
		location_key = getResources().getString(R.string.location_key);
		update_frequency_key = getResources().getString(R.string.autoupdate_frequency_key);
		
		listPreference1 = (ListPreference)findPreference(location_key);
		listPreference2 = (ListPreference)findPreference(update_frequency_key);
		checkBoxPreference = (CheckBoxPreference)findPreference(auto_update_key);
		
		
		otaPreference = findPreference("ota");
		otaPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				progressDialog = ProgressDialog.show(SettingActivity.this,"檢查更新", "檢查中...");
				progressDialog.setCancelable(true);
				new Thread(new Runnable() {
						@Override
						public void run() {
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
									progressDialog.dismiss();
									if(temp_version > version) {
										Message msg = new Message();
										msg.what = 3;
										mHandler.sendMessage(msg);
									}
									else {
										Message msg = new Message();
										msg.what = 4;
										mHandler.sendMessage(msg);
									}
								}
								else {
									Message msg = new Message();
									msg.what = 2;
									mHandler.sendMessage(msg);
								}
							} catch (IOException e) {
								System.out.println("IO");
							} catch (URISyntaxException e) {
								System.out.println("URL");
							}
							Message msg = new Message();
							msg.what = 1;
							mHandler.sendMessage(msg);
					}
				}).start();
        		
        		return true;
			}
		});
		
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				Toast.makeText(getApplicationContext(), "沒有網路", Toast.LENGTH_SHORT).show();
				break;
			case 3: //have ota
				View view = View.inflate(SettingActivity.this, R.layout.ota, null);
				TextView versionChange = (TextView) view.findViewById(R.id.textView1);
				versionChange.setText(versionString);
				versionString = " ";
				TextView textView = (TextView) view.findViewById(R.id.textView3);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
				dialog.setTitle("新版本").setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
			case 4: // do not have ota
				View view1 = View.inflate(SettingActivity.this, R.layout.no_ota, null);
				TextView textview = (TextView) view1.findViewById(R.id.textView3);
				textview.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog1 = new AlertDialog.Builder(SettingActivity.this);
				dialog1.setTitle("沒有找到").setView(view1).setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
		if(key.equals(update_frequency_key)) {
				listPreference2.setSummary(listPreference2.getEntry());
				Intent myService = new Intent(this, Notify_Service.class);
				System.out.println("setting:"+Integer.parseInt(listPreference1.getValue()));
				preference.edit().putInt("location", Integer.parseInt(listPreference1.getValue())).commit();
				System.out.println("setting:"+Long.parseLong(listPreference2.getValue())*60*1000);
				preference.edit().putLong("time", Long.parseLong(listPreference2.getValue())*60*1000).commit();
				stopService(myService);
				startService(myService);
		} else if(key.equals(location_key)) {
				listPreference1.setSummary(listPreference1.getEntry());
				Intent myService = new Intent(this, Notify_Service.class);
				System.out.println("setting:"+Integer.parseInt(listPreference1.getValue()));
				preference.edit().putInt("location", Integer.parseInt(listPreference1.getValue())).commit();
				System.out.println("setting:"+Long.parseLong(listPreference2.getValue())*60*1000);
				preference.edit().putLong("time", Long.parseLong(listPreference2.getValue())*60*1000).commit();
				stopService(myService);
				startService(myService);
		} else if (key.equals(auto_update_key)) {
			if(preference.getBoolean(auto_update_key, false)) {
				Intent myService = new Intent(this, Notify_Service.class);
				System.out.println("setting:"+Integer.parseInt(listPreference1.getValue()));
				preference.edit().putInt("location", Integer.parseInt(listPreference1.getValue())).commit();
				System.out.println("setting:"+Long.parseLong(listPreference2.getValue())*60*1000);
				preference.edit().putLong("time", Long.parseLong(listPreference2.getValue())*60*1000).commit();
				stopService(myService);
				startService(myService);
			} else {
				Intent myService = new Intent(this, Notify_Service.class);
				stopService(myService);
			}
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.setting, menu);
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"返回");
		menu.add(0,1,0,"關於");
		menu.add(0,2,0,"離開");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id) {
			case 0:
				Intent intent = new Intent();
				intent.setClass(SettingActivity.this, MainActivity.class);
				startActivity(intent);
				this.finish();
				break;
			case 1:
				View view = View.inflate(SettingActivity.this, R.layout.about, null); 
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
			case 2:
				SysApplication.getInstance().exit(); 
				break;
		}
		return true;

	}


}
