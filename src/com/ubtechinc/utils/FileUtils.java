package com.ubtechinc.utils;

import android.os.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
   private static final String ENCODE_UTF8 = "UTF-8";
   private static final File sdDir = Environment.getExternalStorageDirectory();

   public FileUtils() {
   }

   public static void writeToProfile(String fileName, String xml) throws IOException {
      String path = sdDir.getPath() + "/actions/" + fileName;
      File f = new File(path);
      if (!f.exists()) {
         f.getParentFile().mkdirs();
         f.createNewFile();
      }

      OutputStream output = new FileOutputStream(f);
      output.write(xml.getBytes());
      output.close();
   }

   public static String readFromProfile(String fileName) throws IOException {
      String path = sdDir.getPath() + "/actions/" + fileName;
      File f = new File(path);
      if (!f.exists()) {
         f.getParentFile().mkdirs();
         f.createNewFile();
         return null;
      } else {
         InputStream intput = new FileInputStream(f);
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         byte[] data = new byte[1024];
         boolean var6 = true;

         int count;
         while((count = intput.read(data, 0, 1024)) != -1) {
            outStream.write(data, 0, count);
         }

         byte[] data = null;
         String xmls = new String(outStream.toByteArray(), "UTF-8");
         intput.close();
         outStream.close();
         return xmls;
      }
   }
}
