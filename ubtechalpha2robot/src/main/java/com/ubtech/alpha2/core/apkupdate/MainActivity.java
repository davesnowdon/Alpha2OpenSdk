package com.ubtech.alpha2.core.apkupdate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.ubtech.alpha2.FilePath;
// ISSUE-1 Layout classes not present in original SDK jar
//import com.ubtech.alpha2.R.layout;
import java.io.File;

public class MainActivity extends Activity {
   Context context;

   public MainActivity() {
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // ISSUE-1 Layout classes not present in original SDK jar
      //this.setContentView(layout.activity_main);
   }

   public void onTest(View view) {
      this.onSlience();
   }

   public void onUpdate() {
      Intent intent = new Intent("android.intent.action.VIEW");
      Log.e("zdy", "onUpdate");
      String path = FilePath.appPath + "/zdyDebug.apk";
      intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
      this.startActivity(intent);
   }

   public void onSlience() {
      Thread installThread = new Thread(new Runnable() {
         public void run() {
            String path = FilePath.appPath + "/zdyDebug.apk";
            InstallerUtils installerUtils = new InstallerUtils();
            installerUtils.install(path);
         }
      });
      installThread.start();
   }

   public void onUnInitall() {
      Thread unInstallThread = new Thread(new Runnable() {
         public void run() {
            String busybox = "mount -o remount rw /data";
            String chmod = "chmod 777 /data/app/com.example.zdydebug.apk";
            String uninstallapk = "pm uninstall com.example.zdydebug";
            UnInstallerUtils unInstallerUtils = new UnInstallerUtils();
            unInstallerUtils.chmodApk(busybox, chmod);
            unInstallerUtils.uninstallApk(uninstallapk);
         }
      });
      unInstallThread.start();
   }
}
