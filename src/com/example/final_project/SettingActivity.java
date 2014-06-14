package com.example.final_project;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	String auto_update_key;
	String location_key;
	String update_frequency_key;
	CheckBoxPreference checkBoxPreference;
	ListPreference listPreference1;
	ListPreference listPreference2;
	
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
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
		if(key.equals(update_frequency_key)) {
			listPreference2.setSummary(listPreference2.getEntry());
			Intent myService = new Intent(getApplicationContext() , Notify_Service.class);
			stopService(myService);
			startService(myService);
		} else if(key.equals(location_key)) {
			listPreference1.setSummary(listPreference1.getEntry());
			Intent myService = new Intent(getApplicationContext() , Notify_Service.class);
			stopService(myService);
			startService(myService);
		} else if (key.equals(auto_update_key)) {
			if(preference.getBoolean(auto_update_key, false)) {
				AlarmManager alarms=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
				Intent myService = new Intent(this, Notify_Service.class);
				PendingIntent pendingIntent=PendingIntent.getService(this,0,myService,0);
				alarms.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60*60*1000, pendingIntent);
			} else {
				AlarmManager alarms=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
				Intent myService = new Intent(this, Notify_Service.class);
				PendingIntent pendingIntent=PendingIntent.getService(this,0,myService,0);
				alarms.cancel(pendingIntent); 
				stopService(myService);
			}
		}
	}
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.setting, menu);
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"��^");
		menu.add(0,1,0,"����");
		menu.add(0,2,0,"���}");
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
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("����").setMessage("��ƨӷ��G��F�|��l�෽�e���|").setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
