package com.ubtech.alpha2.core.unzip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

public class Unzip {
   private static final int buffer = 2048;
   private Unzip.UnzipListener listener;

   public Unzip(Unzip.UnzipListener listener) {
      this.listener = listener;
   }

   public void unZip(final String path, final String des) {
      (new Thread() {
         public void run() {
            File f = new File(path);
            if (!f.exists()) {
               Unzip.this.listener.onResult(0);
            } else {
               int code = 1;
               String savepath = "";
               File file = null;
               InputStream is = null;
               FileOutputStream fos = null;
               BufferedOutputStream bos = null;
               savepath = des + File.separator;
               (new File(savepath)).mkdir();
               ZipFile zipFile = null;

               try {
                  zipFile = new ZipFile(path, "gbk");
                  Enumeration entries = zipFile.getEntries();

                  while(true) {
                     while(entries.hasMoreElements()) {
                        byte[] buf = new byte[2048];
                        ZipEntry entry = (ZipEntry)entries.nextElement();
                        String filename = entry.getName();
                        boolean ismkdir = false;
                        if (filename.lastIndexOf("/") != -1) {
                           ismkdir = true;
                        }

                        filename = savepath + filename;
                        if (entry.isDirectory()) {
                           file = new File(filename);
                           file.mkdirs();
                        } else {
                           file = new File(filename);
                           if (!file.exists() && ismkdir) {
                              (new File(filename.substring(0, filename.lastIndexOf("/")))).mkdirs();
                           }

                           file.createNewFile();
                           is = zipFile.getInputStream(entry);
                           fos = new FileOutputStream(file);
                           bos = new BufferedOutputStream(fos, 2048);

                           int countx;
                           while((countx = is.read(buf)) > -1) {
                              bos.write(buf, 0, countx);
                           }

                           bos.flush();
                           bos.close();
                           fos.close();
                           is.close();
                        }
                     }

                     zipFile.close();
                     break;
                  }
               } catch (IOException var23) {
                  var23.printStackTrace();
                  code = 0;
               } finally {
                  try {
                     if (bos != null) {
                        bos.close();
                     }

                     if (fos != null) {
                        fos.close();
                     }

                     if (is != null) {
                        is.close();
                     }

                     if (zipFile != null) {
                        zipFile.close();
                     }

                     Unzip.this.deleteFile(path);
                  } catch (Exception var22) {
                     var22.printStackTrace();
                  }

               }

               Unzip.this.listener.onResult(code);
            }
         }
      }).start();
   }

   public void deleteFile(String sPath) {
      File file = new File(sPath);
      if (file.isFile() && file.exists()) {
         file.delete();
      }

   }

   public static void main(String[] args) {
      (new Unzip(new Unzip.UnzipListener() {
         public void onResult(int code) {
         }
      })).unZip("讯飞舞蹈1.zip", "test");
   }

   public interface UnzipListener {
      void onResult(int var1);
   }
}
