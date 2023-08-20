package com.ubtech.alpha2.core.apkupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateOverReceiver extends BroadcastReceiver {
   public UpdateOverReceiver() {
   }

   public void onReceive(Context context, Intent arg1) {
      Log.i("zdy", "更新完成再次启动");
   }
}
