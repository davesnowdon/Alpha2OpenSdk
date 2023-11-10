package com.ubtech.alpha2.core.apkupdate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InstallerUtils {
   public InstallerUtils() {
   }

   public String install(String apkAbsolutePath) {
      String[] args = new String[]{"pm", "install", "-r", apkAbsolutePath};
      String result = "";
      ProcessBuilder processBuilder = new ProcessBuilder(args);
      Process process = null;
      InputStream errIs = null;
      InputStream inIs = null;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         int read = true;
         process = processBuilder.start();
         errIs = process.getErrorStream();

         int read;
         while((read = errIs.read()) != -1) {
            baos.write(read);
         }

         baos.write(157);
         inIs = process.getInputStream();

         while((read = inIs.read()) != -1) {
            baos.write(read);
         }

         byte[] data = baos.toByteArray();
         result = new String(data);
      } catch (IOException var21) {
         var21.printStackTrace();
      } catch (Exception var22) {
         var22.printStackTrace();
      } finally {
         try {
            if (errIs != null) {
               errIs.close();
            }

            if (inIs != null) {
               inIs.close();
            }
         } catch (IOException var20) {
            var20.printStackTrace();
         }

         if (process != null) {
            process.destroy();
         }

      }

      return result;
   }
}
