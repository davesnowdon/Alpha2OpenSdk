package com.ubtech.alpha2.core.apkupdate;

import java.io.DataOutputStream;

public class UnInstallerUtils {
   public UnInstallerUtils() {
   }

   public void chmodApk(String busybox, String chmod) {
      try {
         Process process = null;
         DataOutputStream os = null;
         process = Runtime.getRuntime().exec("sh");
         os = new DataOutputStream(process.getOutputStream());
         os.writeBytes(busybox);
         os.flush();
         os.writeBytes(chmod);
         os.flush();
         os.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public boolean uninstallApk(String uninstallapk) {
      boolean ret = false;

      try {
         Process process = null;
         DataOutputStream os = null;
         process = Runtime.getRuntime().exec("sh");
         os = new DataOutputStream(process.getOutputStream());
         os.writeBytes(uninstallapk);
         os.flush();
         os.close();
         int value = process.waitFor();
         if (value == 0) {
            ret = true;
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         ret = false;
      }

      return ret;
   }
}
