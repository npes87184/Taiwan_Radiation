package com.example.final_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
		if(key.equals(update_frequency_key)) {
			listPreference2.setSummary(listPreference2.getEntry());
			System.out.println(listPreference1.getEntry());
		} else if(key.equals(location_key)) {
			listPreference1.setSummary(listPreference1.getEntry());
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
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("關於").setMessage("資料來源：行政院原子能源委員會").setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
