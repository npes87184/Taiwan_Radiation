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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class Notify_Service extends Service {
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
					String line = reader.readLine();
					for(int i=0;i<6;i++) {
						line = reader.readLine();
					}
					String [] data = line.split(",");
					if(Float.parseFloat(data[2]) >= 0.003) {
						CharSequence tickerText = "輻射超標";
						Notification notification = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
						
						int NOTIFICATION_ID = 0;

					    Intent targetIntent = new Intent(getApplicationContext(), MainActivity.class);
					    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					    notification.setLatestEventInfo(getApplicationContext(), "輻射超標",data[0] + "的輻射超標了！", contentIntent);
					    NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					    notification.flags = Notification.FLAG_AUTO_CANCEL;
					    nManager.notify(NOTIFICATION_ID, notification);
					}
				} catch(IOException e) {
					
				} catch (URISyntaxException e) {
					
				}
			}
		}).start();
	    return Service.START_REDELIVER_INTENT;
	}
	
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
    
	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.aec.gov.tw/open/gammamonitor.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
}



