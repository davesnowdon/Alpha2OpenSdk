package com.ubtech.alpha2.core.apkupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class Test {
   public Test() {
   }

   public static class TestBroadcastReceiver extends BroadcastReceiver {
      public TestBroadcastReceiver() {
      }

      public void onReceive(Context context, Intent intent) {
         PackageManager manager = context.getPackageManager();
         String packageName;
         if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            packageName = intent.getData().getSchemeSpecificPart();
            Log.e("zdy", "安装成功" + packageName);
         }

         if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            packageName = intent.getData().getSchemeSpecificPart();
            Log.e("zdy", "卸载成功" + packageName);
         }

         if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            packageName = intent.getData().getSchemeSpecificPart();
            Log.e("zdy", "替换成功" + packageName);
            Intent intent2 = new Intent("android.intent.action.PACKAGE_REPLACED_OVER");
            context.sendBroadcast(intent2);
         }

      }
   }
}
