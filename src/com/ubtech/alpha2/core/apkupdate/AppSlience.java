package com.ubtech.alpha2.core.apkupdate;

import java.io.File;

public class AppSlience {
   public AppSlience() {
   }

   public void onSlience(final String path, final AppSlience.SlienceListener listener) {
      Thread installThread = new Thread(new Runnable() {
         public void run() {
            InstallerUtils installerUtils = new InstallerUtils();
            String ret = installerUtils.install(path);
            if (ret.toLowerCase().contains("success")) {
               listener.onSlienceResult(1);
            } else {
               listener.onSlienceResult(0);
            }

            AppSlience.this.deleteFile(path);
         }
      });
      installThread.start();
   }

   public void deleteFile(String sPath) {
      File file = new File(sPath);
      if (file.isFile() && file.exists()) {
         file.delete();
      }

   }

   public void onUnInitall(final String app, final AppSlience.SlienceListener listener) {
      Thread unInstallThread = new Thread(new Runnable() {
         public void run() {
            String busybox = "mount -o remount rw /data";
            String chmod = "chmod 777 /data/app/" + app + ".apk";
            String uninstallapk = "pm uninstall " + app;
            UnInstallerUtils unInstallerUtils = new UnInstallerUtils();
            unInstallerUtils.chmodApk(busybox, chmod);
            boolean ret = unInstallerUtils.uninstallApk(uninstallapk);
            if (ret) {
               listener.onSlienceResult(1);
            } else {
               listener.onSlienceResult(0);
            }

         }
      });
      unInstallThread.start();
   }

   public interface SlienceListener {
      void onSlienceResult(int var1);
   }
}
