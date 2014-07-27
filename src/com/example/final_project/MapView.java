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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MapView extends Activity {

	private static final String MAP_URL = "file:///android_asset/new.html";
	private WebView webView;
	private boolean webviewReady = false; 
	private String [] inputStrings = new String[45];
	
	
	@JavascriptInterface
	public String getData(int i) {
		System.out.println("JS success");
		System.out.println(inputStrings[i]);
		return inputStrings[i];
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		SysApplication.getInstance().addActivity(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				refreshMap();
			}
		}).start();
		setupWebView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu);
		menu.add(0,0,0,"重新整理");
		menu.add(0,1,0,"列表顯示");
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
				Intent intent = new Intent();
				intent.setClass(MapView.this, MapView.class);
				startActivity(intent);
				finish();
				break;
			case 1:
				Intent intent2 = new Intent();
				intent2.setClass(MapView.this, MainActivity.class);
				startActivity(intent2);
				finish();
				break;
			case 2:
				Intent intent1 = new Intent();
				intent1.setClass(MapView.this, SettingActivity.class);
				startActivity(intent1);
				break;
			case 3:
				View view = View.inflate(MapView.this, R.layout.about, null); 
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
	

	//Download main function.
	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.aec.gov.tw/open/gammamonitor.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
	private void refreshMap() {
		try {
			boolean first = true;
			int i = 0;
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = CM.getActiveNetworkInfo();
			if((info != null) && info.isConnected()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
				String line;
				while((line = reader.readLine())!=null) {
					String [] data = line.split(",");
					if(first) {
						first = false;
						continue;
					}
					inputStrings[i]=data[2];
				//	System.out.println(inputStrings[i]);
					i++;
				}
			}
			else {
				System.exit(0);
			}
		} catch (IOException e) {
			refreshMap();
		} catch (URISyntaxException e) {
			refreshMap();
		}
	}
	
	private void setupWebView() {
		 webView = (WebView) findViewById(R.id.webView1);
		 webView.getSettings().setJavaScriptEnabled(true);
		
		 //Wait for the page to load then send the location information
		 webView.setWebViewClient(new WebViewClient(){ 
			 @Override 
			 public void onPageFinished(WebView view, String url) {
				 //webView.loadUrl(centerURL);
				 webviewReady = true;//webview已經載入完畢
				 webView.loadUrl("javascript:resizeMap("+ (webView.getHeight()) + ")");
			 }
		  });
		     webView.loadUrl(MAP_URL);     
		     webView.addJavascriptInterface(MapView.this , "AndroidFunction");
	}
}
