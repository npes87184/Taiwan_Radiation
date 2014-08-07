package com.example.final_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receive_BootCompleted extends BroadcastReceiver {
	
	@Override
    public void onReceive(Context context, Intent intent) {
       //we double check here for only boot complete event
		
       if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
    		   Intent serviceIntent = new Intent(context, Notify_Service.class); 
    		   context.startService(serviceIntent);
      }
   }
	

	
}
