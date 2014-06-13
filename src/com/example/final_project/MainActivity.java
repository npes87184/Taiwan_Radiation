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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnRefreshListener {

	boolean first = true;
	private RefreshNowListView listView;
	ArrayAdapter<String> adapter;
	ArrayList<String> list = new ArrayList<String>();
	Time t = new Time();
	private boolean refreshing = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SysApplication.getInstance().addActivity(this);
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
	
	public void refresh() {
		try {
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = CM.getActiveNetworkInfo();
			if((info != null) && info.isConnected()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
				t.setToNow();
				list.clear();
				list.add("��s�ɶ��G" + t.hour+ ":" + t.minute + ":" + t.second);
				list.add("�a�I  �ʴ���(�L�襱/��)");
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
				list.add("�S�������A�ж}�Һ����᭫�s�Ұ����ΡC");
			}
			first = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	
	//google style refresh
	@Override
	public void onRefreshComplete() {
		Toast.makeText(this, "���s��z����", Toast.LENGTH_SHORT).show();
	//	setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onRefreshStart(final RefreshMode mode) {
	//	Toast.makeText(this, "���s��z��", Toast.LENGTH_SHORT).show();
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
		menu.add(0,0,0,"���s��z");
		menu.add(0,1,0,"�]�w");
		menu.add(0,2,0,"����");
		menu.add(0,3,0,"���}");
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
					Toast.makeText(this, "���s��z����", Toast.LENGTH_SHORT).show();
					refreshing = false;
				}
				break;
			case 1:
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, SettingActivity.class);
				startActivity(intent);
				break;
			case 2:
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("����").setMessage("��ƨӷ��G��F�|��l�෽�e���|").setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
			case 3:
				SysApplication.getInstance().exit(); 
				break;
		}
		return true;
	}
}
	
